package io.beacon.geofenceservice.service;

import io.beacon.events.LocationEvent;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.events.GeofenceNotificationEvent;
import io.beacon.geofenceservice.events.GeofenceNotificationEventProducer;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

  private final GeofenceRepository geofenceRepository;
  private final double EVENT_TRIGGER_TYPE_NEAR_METERS = 30.0;
  private final GeofenceNotificationEventProducer notificationEventProducer;

  public Mono<Void> processLocationEvent(LocationEvent event) {
    return Mono.fromCallable(() ->
            geofenceRepository.findRelatedGeofences(
                event.longitude(),
                event.latitude(),
                EVENT_TRIGGER_TYPE_NEAR_METERS))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .flatMap(this::handleRelatedGeofenceEvent)
        .then();
  }

  private Mono<Void> handleRelatedGeofenceEvent(Geofence geofence) {
    GeofenceNotificationEvent notification = new GeofenceNotificationEvent(
        geofence.getUserId().toString(),
        geofence.getTargetId().toString(),
        geofence.getTriggerType(),
        geofence.getId().toString()
    );
    return notificationEventProducer.send(notification);
  }
}
