package io.beacon.historyservice.locations.service;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.exceptions.InvalidTimeRangeException;
import io.beacon.historyservice.locations.dto.LocationHistoryResponse;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.mappers.LocationHistoryMapper;
import io.beacon.historyservice.locations.mappers.LocationMapper;
import io.beacon.historyservice.locations.repository.LocationHistoryRepository;
import io.beacon.security.utils.AuthUtils;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationHistoryService {

  private final LocationHistoryRepository repository;
  private final LocationHistoryMapper mapper;

  public Mono<LocationHistory> persistLocationEvent(LocationEvent event) {
    LocationHistory history = LocationMapper.toLocationHistory(event);
    return Mono.fromCallable(() -> repository.save(history))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(e -> log.error("Failed to persist location history for id {}",
            history.getId(), e));
  }

  public Mono<Set<LocationHistoryResponse>> fetchRecents(Integer limit) {
    Mono<UUID> futureUserId = AuthUtils.getCurrentUserId();
    return futureUserId.publishOn(Schedulers.boundedElastic())
        .map(userId -> repository.findRecents(userId, Limit.of(limit))
            .stream()
            .map(mapper::toLocationHistoryResponse)
            .collect(
                Collectors.toSet()));
  }

  public Mono<Set<LocationHistoryResponse>> fetchBetween(Instant start, Instant end, Sort.Direction direction) {
    if (end.isBefore(start)) {
      return Mono.error(new InvalidTimeRangeException("'end' must be after or equal to 'start'"));
    }
    Mono<UUID> futureUserId = AuthUtils.getCurrentUserId();
    return futureUserId.publishOn(Schedulers.boundedElastic())
        .map(userId -> repository.findById_UserIdAndId_TimestampBetween(userId, start, end, Sort.by(direction, "id.timestamp"))
            .stream()
            .map(mapper::toLocationHistoryResponse).collect(Collectors.toSet()));
  }
}
