package io.beacon.historyservice.locations.stream;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.service.LocationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {

  private  final LocationHistoryService historyService;
  
  @KafkaListener(topics = "user-location-events", containerFactory = "locationsKafkaListenerContainerFactory")
  public Mono<Void> listen(LocationEvent event) {
    log.info("Received location event: {} ", event);
    return 
  }
}
