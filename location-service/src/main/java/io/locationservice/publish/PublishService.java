package io.locationservice.publish;

import io.locationservice.request.PublishLocationRequest;
import io.locationservice.utils.CacheUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@AllArgsConstructor
public class PublishService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public Mono<RecordId> publish(PublishLocationRequest input) {
        String recordKey = CacheUtils.buildLocationRecordKey(input.userId());
        RecordId recordId = CacheUtils.buildLocationRecordId(input.capturedAt());
        Map<String, Double> fields = Map.of("lat", input.coords().latitude(), "lon", input.coords().longitude());
        MapRecord<String, String, Double> record = StreamRecords.newRecord()
                .in(recordKey)
                .withId(recordId)
                .ofMap(fields);
        return redisTemplate.opsForStream().add(record);
    }

}
