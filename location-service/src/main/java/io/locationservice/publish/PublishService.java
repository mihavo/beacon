package io.locationservice.publish;

import io.locationservice.request.PublishLocationRequest;
import io.locationservice.utils.CacheUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
public class PublishService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public Flux<RecordId> publish(Set<PublishLocationRequest> input) {
        return Flux.fromIterable(input).flatMap(request -> {
            String recordKey = CacheUtils.buildLocationRecordKey(request.userId());
            RecordId recordId = CacheUtils.buildLocationRecordId(request.capturedAt());
            Map<String, Double> fields = Map.of("lat", request.coords().latitude(), "lon", request.coords().longitude());
            MapRecord<String, String, Double> record = StreamRecords.newRecord()
                    .in(recordKey)
                    .withId(recordId)
                    .ofMap(fields);
            return redisTemplate.opsForStream().add(record);
        });
    }

}
