package io.beacon.events;

import io.beacon.events.enums.TriggerType;
import java.time.Instant;

public record GeofenceNotificationEvent(
    String producerUserId,
    String targetUserId,
    TriggerType triggerType,
    String geofenceId,
    Instant timestamp
) {
}
