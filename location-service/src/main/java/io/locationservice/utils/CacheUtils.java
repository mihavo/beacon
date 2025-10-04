package io.locationservice.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.redis.connection.stream.RecordId;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class CacheUtils {

    public String buildLocationRecordKey(UUID userId) {
        return String.format("locations:%s", userId);
    }

    public RecordId buildLocationRecordId(Instant timestamp) {
        return RecordId.of(timestamp.toEpochMilli(), timestamp.getNano() / 1000);
    }
}
