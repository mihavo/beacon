package io.beacon.locationservice.location.events;

import io.beacon.events.LocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventsProducer {

  private final KafkaTemplate<String, LocationEvent> template;

  public Mono<Void> send(LocationEvent event) {
    return Mono.fromFuture(
        () -> template.send("user-location-events", event.userId(), event)).doOnSuccess(
        result -> log.info("Sent location event for user {} at: {}",
            event.userId(), result.getRecordMetadata().timestamp())).then();
  }
}
