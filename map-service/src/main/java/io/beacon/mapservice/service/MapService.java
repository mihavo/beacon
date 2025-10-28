package io.beacon.mapservice.service;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.router.EventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
  private final EventRouter router;

  public Mono<Void> processLocation(LocationEvent event) {
    return router.dispatch(event)
        .doOnSuccess(v -> log.debug("Dispatched location event for user {}", event.userId()))
        .doOnError(e -> log.error("Failed to dispatch location event for user {}", event.userId(), e));
  }
}
