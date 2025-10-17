package io.beacon.locationservice.location.eviction;

import io.beacon.events.LocationEvent;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.location.events.LocationEventsProducer;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.utils.CacheUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvictionService {
  
  private final ReactiveRedisTemplate<String,Object> redisTemplate;
  private final LocationEventsProducer locationEventsProducer;

  @Value("${io.beacon.cache.maxStreamEntries}")
  private Integer maxStreamEntries;
  
  
    @Value("${io.beacon.cache.streamTrimRatio}")
  private Double streamTrimRatio;

  @Value("${io.beacon.cache.mergeDistanceThreshold}")
    private Integer mergeDistanceThreshold;
    
    private static final SpatialContext GEO = SpatialContext.GEO;
    
    
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
  public Mono<Void> evaluateEviction(UUID userId) {
    String key = CacheUtils.buildLocationStreamKey(userId);
    return redisTemplate.opsForStream().size(key)
         .filter(size -> size >= maxStreamEntries)
        .flatMap(size -> trimStream(key, size))
        .flatMapMany(Flux::fromIterable)
        .concatMap(location -> {
          LocationEvent event = LocationMapper.toLocationEvent(userId, location);
          return locationEventsProducer.send(event).timeout(Duration.ofSeconds(5))
              .onErrorResume(ex -> {
                log.error("Send timed out", ex);
                return Mono.empty();
              })
              .then(flushFromCache(key, location));
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
  private Mono<List<Location>> trimStream(String key, Long streamSize) {
    int chunkSize = Math.max(1, (int) (streamSize * streamTrimRatio));
    return redisTemplate.<String, Object>opsForStream().range(key, Range.unbounded(),
            Limit.limit().count(chunkSize))
        .collectList().flatMap(records -> {
          List<Location> locations = records.stream().map(LocationMapper::toLocation).toList();
          return mergeCloseCoordinates(locations);
        });
  }

  /**
   * Flushes the provided location from the cache of the provided key.
   *
   * @param key      the stream key
   * @param location the location
   * @return the number of removed records
   */
  private Mono<Long> flushFromCache(String key, Location location) {
    return redisTemplate.opsForStream().delete(key, location.getTimestamp().toString());
  }

  /**
   * Receives a list of locations, and merges those that are close based on the merge distance
   * threshold
   *
   * @param locations the locations to merge
   * @return the merged locations list
   */
  private Mono<List<Location>> mergeCloseCoordinates(List<Location> locations) {
    List<Location> merged = new ArrayList<>();
      ShapeFactory shapeFactory = GEO.getShapeFactory();
    for(Location location : locations) {
      Point p1 =
          shapeFactory.pointXY(location.getCoords().longitude(), location.getCoords()
              .latitude());
              Optional<Location> possibleCloseLocation = merged.stream()
            .filter(m -> {
                Point p2 = shapeFactory.pointXY(m.getCoords().longitude(), m.getCoords().latitude());
                double distanceKm = GEO.getDistCalc().distance(p1, p2) * DistanceUtils.DEG_TO_KM;
                return distanceKm * 1000 < mergeDistanceThreshold;
            })
            .findFirst();
              if(possibleCloseLocation.isPresent()) {
                Location closeLocation = possibleCloseLocation.get();
                            double avgLat = (location.getCoords().latitude() + closeLocation.getCoords().latitude()) / 2;
            double avgLon = (location.getCoords().longitude() + closeLocation.getCoords().longitude()) / 2;
            closeLocation.setCoords(new Coordinates(avgLat,avgLon));
              }
              else {
                merged.add(location);
              }
      
    }
    return Mono.just(merged);
  }
}
