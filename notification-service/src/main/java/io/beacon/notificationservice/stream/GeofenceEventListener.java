package io.beacon.notificationservice.stream;

import io.beacon.events.GeofenceNotificationEvent;
import io.beacon.notificationservice.messages.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeofenceEventListener {

  private final MessageService messageService;

  @KafkaListener(topics = "geofence-alerts", containerFactory = "geofenceNotificationsKafkaListenerContainerFactory")
  public Mono<Void> listen(GeofenceNotificationEvent event) {
    log.debug("Received geofence alert: {}", event);
    return messageService.sendGeofenceNotificationMessage(event).then();
  }
}
