package io.beacon.mapservice.stream;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {

  private final MapService mapService;

  @KafkaListener(topics = "location-history-events", containerFactory = "locationsKafkaListenerContainerFactory")
  public Mono<Void> listen(LocationEvent event) {
    log.debug("Received location event: {}", event);
    return mapService.processLocation(event);
  }
}
