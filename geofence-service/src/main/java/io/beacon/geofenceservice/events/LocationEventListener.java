package io.beacon.geofenceservice.events;

import io.beacon.events.LocationEvent;
import io.beacon.geofenceservice.service.EventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {

  private final EventsService eventsService;

  @KafkaListener(topics = "location-stream-events", containerFactory = "locationsKafkaListenerContainerFactory")
  public Mono<Void> listen(LocationEvent event) {
    log.debug("Received location event: {}", event);
    return eventsService.processLocationEvent(event);
  }
}
