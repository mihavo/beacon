package io.beacon.events;

import io.beacon.events.enums.TriggerType;

public record GeofenceNotificationEvent(
    String producerUserId,
    String targetUserId,
    TriggerType triggerType,
    String geofenceId
) {
}
