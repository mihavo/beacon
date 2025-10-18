package io.beacon.locationservice.utils;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.experimental.UtilityClass;
import org.springframework.data.redis.connection.stream.RecordId;

@UtilityClass
public class CacheUtils {

  private final ConcurrentHashMap<String, AtomicLong> sequenceMap = new ConcurrentHashMap<>();

  public String buildLocationStreamKey(UUID userId) {
        return String.format("locations:%s", userId);
    }

    public RecordId buildLocationRecordId(Instant timestamp) {
      long milli = timestamp.toEpochMilli();
      AtomicLong counter = sequenceMap.computeIfAbsent(String.valueOf(milli),
          k -> new AtomicLong(0));
      long seq = counter.getAndIncrement();
      sequenceCleanup();
      return RecordId.of(milli, seq);
    }

  public UUID extractUserId(String recordKey) {
        return UUID.fromString(recordKey.substring("locations:".length()));
    }

    public Instant extractCapturedAtTimestamp(String recordId) {
        long millis = Long.parseLong(recordId.split("-")[0]);
        long nanos = Long.parseLong(recordId.split("-")[1]);
        return Instant.ofEpochMilli(millis).plusNanos(nanos);
    }

  public String getFriendshipListKey(String userId) {
    return String.format("users:%s:friends", userId);
  }

  /**
   * Removes all entries older than 60 sec, if the sequence map grows large
   */
  private static void sequenceCleanup() {
    if (sequenceMap.size() > 1000) {
      sequenceMap.entrySet().removeIf(e ->
          Long.parseLong(e.getKey()) < System.currentTimeMillis() - 60000);
    }
  }
}
