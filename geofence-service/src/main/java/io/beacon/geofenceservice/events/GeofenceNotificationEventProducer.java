package io.beacon.geofenceservice.events;

import io.beacon.events.GeofenceNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeofenceNotificationEventProducer {

  private final KafkaTemplate<String, GeofenceNotificationEvent> template;

  public Mono<Void> send(GeofenceNotificationEvent event) {
    return Mono.fromFuture(() -> template.send(event.geofenceId(), event)).doOnSuccess(
        result -> log.info("Sent geofence notification event of trigger type {} , producer user {}, target user {} at {}",
            event.triggerType(), event.producerUserId(), event.targetUserId(), result.getRecordMetadata().timestamp())).then();
  }
}
