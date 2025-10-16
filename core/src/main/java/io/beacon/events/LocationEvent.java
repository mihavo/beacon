package io.beacon.events;

import java.time.Instant;

public record LocationEvent(
    String userId,
    Double latitude,
    Double longitude,
    Instant timestamp
) {
}
