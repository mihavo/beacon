package io.beacon.locationservice.publish;

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
                .doOnNext(id -> log.info("Published location {}", id))
                .flatMap(id -> evictionService.evaluateEviction(userId).thenReturn(id));
          });

      PublishLocationRequest lastLocation = input.stream()
          .max(Comparator.comparing(PublishLocationRequest::capturedAt))
          .orElseThrow();

      Mono<Long> geoUpdate = redisTemplate.opsForGeo()
          .add(CacheUtils.getLocationGeospatialKey(),
              new Point(lastLocation.coords().longitude(), lastLocation.coords().latitude()),
              CacheUtils.buildGeospatialMember(userId, lastLocation.capturedAt()));

      return streamRecords.collectList()
          .flatMapMany(records -> geoUpdate.thenMany(Flux.fromIterable(records)));
    });
  }
}

