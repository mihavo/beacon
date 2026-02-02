package io.beacon.locationservice.publish;

import io.beacon.events.LocationEvent;
import io.beacon.locationservice.location.events.LocationEventsProducer;
import io.beacon.locationservice.location.eviction.EvictionService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.CacheUtils;
import io.beacon.security.utils.AuthUtils;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class PublishService {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;
  private final EvictionService evictionService;
  private final LocationEventsProducer locationEventsProducer;
  private final ReactiveRedisTemplate<String, String> valueRedisTemplate;

  /**
   * Receives a collection of {@link PublishLocationRequest}s and
   * from the client that represent recent location captures of the users.
   * It stores them in the user's dedicated redis stream. The location's metadata (last known location + timestamp)
   * are also stored in the redis datastore. Evaluate eviction is run at the end using the {@link EvictionService}.
   *
   * @param input the collection of location records
   * @return the new record ids in the redis datastore
   */
  public Flux<RecordId> publish(Set<PublishLocationRequest> input) {
    Mono<UUID> futureUserId = AuthUtils.getCurrentUserId();

    return futureUserId.flatMapMany(userId -> {
      Flux<RecordId> streamRecords = Flux.fromIterable(input)
          .flatMap(request -> {
            double lat = request.coords().latitude();
            double lon = request.coords().longitude();

            String streamKey = CacheUtils.buildLocationStreamKey(userId);
            Map<String, Object> fields = Map.of(
                "lat", lat,
                "lon", lon,
                "capturedAt", request.capturedAt()
            );

            MapRecord<String, String, Object> record =
                StreamRecords.newRecord().in(streamKey).ofMap(fields);

            return redisTemplate.opsForStream()
                .add(record)
                .doOnNext(id -> log.info("Published location {}", id));
          });

      PublishLocationRequest lastLocation = input.stream()
          .max(Comparator.comparing(PublishLocationRequest::capturedAt))
          .orElseThrow();

      Mono<Long> geoUpdate = valueRedisTemplate.opsForGeo()
          .add(CacheUtils.getLocationGeospatialKey(),
              new Point(lastLocation.coords().longitude(), lastLocation.coords().latitude()),
              CacheUtils.buildGeospatialMember(userId));

      Mono<Boolean> timestampUpdate =
          valueRedisTemplate.opsForValue().set(
              CacheUtils.buildTimestampKey(userId),
              lastLocation.capturedAt().toString()
          );

      Mono<Void> streamEvent =
          locationEventsProducer.sendAsStreamEvent(
              new LocationEvent(
                  userId.toString(),
                  lastLocation.coords().latitude(),
                  lastLocation.coords().longitude(),
                  lastLocation.capturedAt()
              )
          );

      return streamRecords.collectList()
          .flatMap(records ->
              Mono.when(geoUpdate, timestampUpdate, streamEvent)
                  .then(evictionService.evaluateEviction(userId))
                  .thenReturn(records)
          )
          .flatMapMany(Flux::fromIterable);
    });
  }
}

