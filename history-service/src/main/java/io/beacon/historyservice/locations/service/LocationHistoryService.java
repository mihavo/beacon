package io.beacon.historyservice.locations.service;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.mappers.LocationMapper;
import io.beacon.historyservice.locations.repository.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationHistoryService {

  private final LocationHistoryRepository repository;

  public Mono<LocationHistory> persistLocationEvent(LocationEvent event) {
    LocationHistory history = LocationMapper.toLocationHistory(event);
    System.out.println("Key is : " + history.getId().getUserId() + "   " + history.getId().getTimestamp());
    return Mono.fromCallable(() -> repository.save(history))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(e -> log.error("Failed to persist location history for id {}",
            history.getId(), e));
  }
}
