package io.beacon.notificationservice.subscriptions.dto;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    String fcmToken,
    UUID deviceId,
    Instant createdAt,
    Instant updatedAt) {
}
