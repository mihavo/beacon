package io.beacon.geofenceservice.dto;

import org.locationtech.jts.geom.Point;

public record GeofenceResponse(
    String id,
    String user_id,
    Point center,
    Double radius_meters) {
}
