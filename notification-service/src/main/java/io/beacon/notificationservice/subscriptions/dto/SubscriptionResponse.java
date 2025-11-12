package io.beacon.notificationservice.subscriptions.dto;

import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    String fcmToken,
    UUID deviceId) {
}
