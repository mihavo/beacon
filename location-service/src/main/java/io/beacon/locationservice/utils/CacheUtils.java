package io.beacon.locationservice.utils;

import java.time.Instant;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheUtils {

  public String buildLocationStreamKey(UUID userId) {
    return String.format("locations:%s", userId);
  }

  public String getLocationGeospatialKey() {
    return "user:locations";
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
}
