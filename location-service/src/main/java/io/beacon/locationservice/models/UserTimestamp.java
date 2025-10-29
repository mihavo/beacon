package io.beacon.locationservice.models;

import java.time.Instant;

//TODO: find a better name for this
public record UserTimestamp(
    String userId,
    Instant timestamp
) {
}
