package io.beacon.geofenceservice.dto;

import io.beacon.geofenceservice.enums.TriggerType;
import jakarta.validation.constraints.NotNull;

public record CreateGeofenceRequest(
    @NotNull(message = "User ID is required")
    String userId,
    @NotNull(message = "Center point longitude is required")
    double centerLongitude,
    @NotNull(message = "Center point latitude is required")
    double centerLatitude,
    @NotNull(message = "Center point radius is required")
    Double radius_meters,
    TriggerType triggerType
) {
}
