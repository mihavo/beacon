package io.beacon.geofenceservice.service;

import io.beacon.events.LocationEvent;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EventsService {

  private final GeofenceRepository geofenceRepository;
  private final double EVENT_TRIGGER_TYPE_NEAR_METERS = 30.0;

  public Mono<Void> processLocationEvent(LocationEvent event) {
    return Mono.fromCallable(() ->
            geofenceRepository.findRelatedGeofences(
                event.longitude(),
                event.latitude(),
                EVENT_TRIGGER_TYPE_NEAR_METERS))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .flatMap(geofence -> handleRelatedGeofenceEvent(geofence, event))
        .then();
  }

  private Mono<Void> handleRelatedGeofenceEvent(Geofence geofence, LocationEvent event) {
    //TODO: produce notification service event
    return Mono.empty();
  }
}
