package io.beacon.notificationservice.subscriptions.dto;

import java.util.List;

public record GetSubscriptionsResponse(
    List<SubscriptionResponse> subscriptions,
    int count
) {
}
