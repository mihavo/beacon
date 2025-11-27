package io.beacon.locationservice.location.eviction;

import io.beacon.events.LocationEvent;
import io.beacon.locationservice.location.events.LocationEventsProducer;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.utils.CacheUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.beacon.locationservice.mappers.LocationMapper.parseCoords;
import static io.beacon.locationservice.mappers.LocationMapper.setCoords;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvictionService {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;
  private final LocationEventsProducer locationEventsProducer;

  @Value("${io.beacon.cache.maxStreamEntries}") private Integer maxStreamEntries;

  @Value("${io.beacon.cache.streamTrimRatio}") private Double streamTrimRatio;

  @Value("${io.beacon.cache.mergeDistanceThreshold}") private Integer mergeDistanceThreshold;

  private static final SpatialContext GEO = SpatialContext.GEO;

  private final Map<UUID, Mono<Void>> evictionLocks = new ConcurrentHashMap<>();

  /**
   * Uses the instance's eviction locks map to handle a set of Monos that subscribe to the runEviction() method.
   * At max 1 subscription will be created for every userId to avoid duplicate evictions.
   *
   * @param userId the userId to run the eviction for
   * @return a subscription for an eviction of a user's locations
   */
  public Mono<Void> evaluateEviction(UUID userId) {
    Mono<Void> next = evictionLocks.compute(userId, (key, current) -> {
          current = current != null ? current : Mono.empty();
          return current.then(runEviction(userId))
              .cache();
        }
    );
    return next.doFinally(signalType ->
        evictionLocks.computeIfPresent(userId, (key, sub) -> sub == next ? null : sub)
    );
  }

  /**
   * Checks if the redis stream with key the provided userId has exceeded the maximum allowed
   * size for that stream. If true, it persists the events, sending them via an event
   * listener
   * to the history service. After confirmation of the event being sent, it flushes these entries
   * from the stream.
   *
   * @param userId
   * @return
   */
  public Mono<Void> runEviction(UUID userId) {
    String key = CacheUtils.buildLocationStreamKey(userId);
    return redisTemplate.opsForStream()
        .size(key)
        .filter(size -> size >= maxStreamEntries)
        .flatMap(size -> trimStream(key, size))
        .flatMapMany(Flux::fromIterable)
        .concatMap(location -> {
          LocationEvent event = LocationMapper.toLocationEvent(userId, location);
          return locationEventsProducer.sendAsHistoryEvent(event).timeout(Duration.ofSeconds(5)).onErrorResume(ex -> {
            log.error("Send timed out", ex);
            return Mono.empty();
          }).then(flushFromCache(location));
        })
        .then();
  }

  /**
   * Collects a chunk of the redis stream (given by the key) and merges the coordinates of the
   * stream's records that are close together
   *
   * @param key        the stream key
   * @param streamSize the stream size (num of records)
   * @return the list of merged locations
   */
  private Mono<List<MapRecord<String, String, Object>>> trimStream(String key, Long streamSize) {
    int chunkSize = Math.max(1, (int) (streamSize * streamTrimRatio));
    return redisTemplate.<String, Object>opsForStream()
        .range(key, Range.unbounded(), Limit.limit().count(chunkSize))
        .collectList()
        .flatMap(this::mergeCloseCoordinates);
  }

  /**
   * Flushes the provided location from the cache of the provided key.
   *
   * @param record the location record
   * @return the number of removed records
   */
  private Mono<Long> flushFromCache(MapRecord<String, String, Object> record) {
    return redisTemplate.opsForStream().delete(record);
  }

  /**
   * Receives a list of stream records, and merges those that are close based on the merge distance
   * threshold
   *
   * @param locations the location records to merge
   * @return the merged records list
   */
  private Mono<List<MapRecord<String, String, Object>>> mergeCloseCoordinates(List<MapRecord<String, String, Object>> locations) {
    List<MapRecord<String, String, Object>> merged = new ArrayList<>();
    ShapeFactory shapeFactory = GEO.getShapeFactory();
    for (MapRecord<String, String, Object> location : locations) {
      Coordinates coords = parseCoords(location);
      Point p1 = shapeFactory.pointXY(coords.longitude(), coords.latitude());
      Optional<MapRecord<String, String, Object>> possibleCloseLocation = merged.stream().filter(m -> {
        Coordinates possibleCoords = parseCoords(m);
        Point p2 = shapeFactory.pointXY(possibleCoords.longitude(), possibleCoords.latitude());
        double distanceKm = GEO.getDistCalc().distance(p1, p2) * DistanceUtils.DEG_TO_KM;
        return distanceKm * 1000 < mergeDistanceThreshold;
      }).findFirst();
      if (possibleCloseLocation.isPresent()) {
        MapRecord<String, String, Object> closeLocation = possibleCloseLocation.get();
        Coordinates closeCoords = parseCoords(closeLocation);
        double avgLat = (coords.latitude() + closeCoords.latitude()) / 2;
        double avgLon = (coords.longitude() + closeCoords.longitude()) / 2;
        setCoords(closeLocation, new Coordinates(avgLat, avgLon));
      } else {
        merged.add(location);
      }
    }
    return Mono.just(merged);
  }
}
