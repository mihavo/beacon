package io.beacon.geofenceservice.dto;

public record GeofenceResponse(
    String id,
    String userId,
    Double centerLongitude,
    Double centerLatitude,
    Double radius_meters) {
}
