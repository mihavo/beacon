package io.beacon.locationservice.models;

import io.beacon.locationservice.entity.Location;

public record UserLocation(
    String userId,
    Location location
) {
}
