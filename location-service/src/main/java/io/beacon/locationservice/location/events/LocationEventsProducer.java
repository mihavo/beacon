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

  public Mono<Void> sendAsHistoryEvent(LocationEvent event) {
    return Mono.fromFuture(
        () -> template.send("location-history-events", event.userId(), event)).doOnSuccess(
        result -> log.debug("Sent location event at history topic for user {} at: {}",
            event.userId(), result.getRecordMetadata().timestamp())).then();
  }

  public Mono<Void> sendAsStreamEvent(LocationEvent event) {
    return Mono.fromFuture(() -> template.send("location-stream-events", event.userId(), event)).doOnSuccess(result -> log.debug(
        "Sent location event to stream topic for user {} at {}", event.userId(), result.getRecordMetadata().timestamp())).then();
  }
}
