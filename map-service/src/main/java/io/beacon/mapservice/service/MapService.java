package io.beacon.mapservice.service;

import io.beacon.events.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MapService {
  public Mono<Void> processLocation(LocationEvent event) {

  }
}
