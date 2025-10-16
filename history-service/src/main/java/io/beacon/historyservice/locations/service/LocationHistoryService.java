package io.beacon.historyservice.locations.service;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.repository.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationHistoryService {

  private final LocationHistoryRepository repository;

  public Mono<LocationHistory> persistLocationEvent(LocationEvent event) {
    return Mono.empty();
  }
}
