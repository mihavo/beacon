package io.beacon.historyservice.locations.service;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.mappers.LocationMapper;
import io.beacon.historyservice.locations.repository.LocationHistoryRepository;
import io.beacon.security.utils.AuthUtils;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
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
    return Mono.fromCallable(() -> repository.save(history))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(e -> log.error("Failed to persist location history for id {}",
            history.getId(), e));
  }

  public Mono<Set<LocationHistory>> fetchRecents(Integer limit) {
    Mono<UUID> futureUserId = AuthUtils.getCurrentUserId();
    return futureUserId.publishOn(Schedulers.boundedElastic()).map(userId -> repository.findRecents(userId, Limit.of(limit)));
  }
}
