package io.beacon.notificationservice.messages;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import io.beacon.events.GeofenceNotificationEvent;
import io.beacon.notificationservice.subscriptions.service.SubscriptionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

  private final SubscriptionService subscriptionService;

  public Mono<Void> sendGeofenceNotificationMessage(GeofenceNotificationEvent event) {
    return subscriptionService.findUserRegistrationToken(UUID.fromString(event.producerUserId())).flatMap(registrationToken -> {
      Message message = Message.builder()
          .putData("type", "GEOFENCE_ALERT")
          .putData("eventType", event.triggerType().name())
          .putData("producerUserId", event.producerUserId())
          .putData("targetUserId", event.targetUserId())
          .putData("geofenceId", event.geofenceId())
          .putData("timestamp", String.valueOf(event.timestamp().toEpochMilli()))
          .setToken(registrationToken)
          .build();
      return FutureUtils.toMono(FirebaseMessaging.getInstance().sendAsync(message))
          .doOnSuccess(response -> log.debug("Sent FCM Message of type GEOFENCE_ALERT for user {}", event.producerUserId()))
          .then();
    }).then();
  }
}
