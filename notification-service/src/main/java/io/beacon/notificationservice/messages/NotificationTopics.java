package io.beacon.notificationservice.messages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationTopics {
  GEOFENCE_ALERTS("geofence-alerts");

  private final String message;
}
