package io.beacon.events;

import io.beacon.geofenceservice.enums.TriggerType;

public record GeofenceNotificationEvent(
    String producerUserId,
    String targetUserId,
    TriggerType triggerType,
    String geofenceId
) {
}
