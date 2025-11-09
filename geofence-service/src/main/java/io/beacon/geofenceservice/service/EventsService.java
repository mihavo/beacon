package io.beacon.geofenceservice.service;

import io.beacon.events.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventsService {

  public Mono<Void> processLocationEvent(LocationEvent event) {
    return null;
  }
}
