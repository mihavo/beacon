package io.beacon.mapservice.models;

import java.time.Instant;

public record UserLocation(
    String userId,
    Coordinates coords,
    Instant timestamp) {
}
