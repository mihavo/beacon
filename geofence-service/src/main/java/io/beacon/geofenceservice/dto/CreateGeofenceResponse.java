package io.beacon.geofenceservice.dto;

import java.util.UUID;

public record CreateGeofenceResponse(
    UUID geofence_id
) {
}
