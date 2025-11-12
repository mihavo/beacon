package io.beacon.notificationservice.subscriptions.dto;

public record SubscriptionRequest(
    String registration_token,
    String device_id
) {
}
