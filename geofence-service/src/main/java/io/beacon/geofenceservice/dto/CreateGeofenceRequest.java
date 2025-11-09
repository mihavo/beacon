package io.beacon.geofenceservice.dto;

import org.locationtech.jts.geom.Point;

public record CreateGeofenceRequest(
    String userId,
    Point center,
    Double radius_meters
) {
}
