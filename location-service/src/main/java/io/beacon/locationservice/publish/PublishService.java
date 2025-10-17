package io.beacon.locationservice.publish;

import io.beacon.locationservice.location.eviction.EvictionService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.CacheUtils;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class PublishService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
  private final EvictionService evictionService;

  public Flux<RecordId> publish(Set<PublishLocationRequest> input) {
        return Flux.fromIterable(input).flatMap(request -> {
          String streamKey = CacheUtils.buildLocationStreamKey(request.userId());
          RecordId recordId = CacheUtils.buildLocationRecordId(request.capturedAt());
            Map<String, Double> fields = Map.of("lat", request.coords().latitude(), "lon", request.coords().longitude());
            MapRecord<String, String, Double> record = StreamRecords.newRecord()
                .in(streamKey)
                    .withId(recordId)
                    .ofMap(fields);
          return redisTemplate.opsForStream()
              .add(record)
              .flatMap(id -> evictionService.evaluateEviction(request.userId())
                  .thenReturn(id));
        });
    }

}
