package io.beacon.geofenceservice.dto;

import io.beacon.geofenceservice.enums.TriggerType;

public record GeofenceResponse(
    String id,
    String userId,
    Double centerLongitude,
    Double centerLatitude,
    Double radius_meters,
    TriggerType triggerType,
    Boolean isActive) {
}
