package io.beacon.mapservice.service;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.mappers.LocationMapper;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.UserLocation;
import io.beacon.mapservice.router.EventRouter;
import io.beacon.security.utils.AuthUtils;
import locationservice.LocationServiceGrpc;
import locationservice.LocationServiceOuterClass;
import locationservice.LocationServiceOuterClass.LatestLocationsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {

  private final EventRouter router;
  private final LocationMapper locationMapper;

  private final LocationServiceGrpc.LocationServiceBlockingStub locationServiceStub;

  public Mono<Void> processLocation(LocationEvent event) {
    return router.dispatch(event)
        .doOnSuccess(v -> log.debug("Dispatched location event for user {}", event.userId()))
        .doOnError(e -> log.error("Failed to dispatch location event for user {}", event.userId(), e));
  }

  public Flux<UserLocation> subscribe(String clientId, BoundingBox bbox) {
    return router.subscribe(clientId, bbox).map(locationMapper::toUserLocation);
  }

  public Flux<UserLocation> getCurrentLocations(BoundingBox boundingBox) {
    LocationServiceOuterClass.BoundingBox bbox =
        LocationServiceOuterClass.BoundingBox.newBuilder()
            .setMinLat(boundingBox.minLat())
            .setMaxLat(boundingBox.maxLat())
            .setMinLon(
                boundingBox.minLon())
            .setMaxLon(boundingBox.maxLon())
            .build();
    return AuthUtils.getCurrentUserId().flatMapMany(userId -> {
      LatestLocationsRequest req =
          LatestLocationsRequest.newBuilder().setRequesterUserId(userId.toString()).setBbox(bbox).build();
    LocationServiceOuterClass.LatestLocationsResponse response = locationServiceStub.getLatestLocations(req);
    return Flux.fromIterable(response.getLocationsList()).map(locationMapper::toUserLocation);
    });
  }
}
