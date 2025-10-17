package io.beacon.locationservice.publish;

import io.beacon.locationservice.location.eviction.EvictionService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.CacheUtils;
import io.beacon.security.utils.AuthUtils;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    return futureUserId.flatMapMany(userId -> Flux.fromIterable(input).flatMap(request -> {
      String streamKey = CacheUtils.buildLocationStreamKey(userId);
      RecordId recordId = CacheUtils.buildLocationRecordId(request.capturedAt());
      Map<String, Double> fields =
          Map.of("lat", request.coords().latitude(), "lon", request.coords().longitude());
      MapRecord<String, String, Double> record =
          StreamRecords.newRecord().in(streamKey).withId(recordId).ofMap(fields);
      return redisTemplate.opsForStream()
          .add(record)
          .doOnNext((publishedId) -> log.info("Published location for record {}", publishedId))
          .flatMap(id -> evictionService.evaluateEviction(userId).thenReturn(id));
    }));
  }

}
