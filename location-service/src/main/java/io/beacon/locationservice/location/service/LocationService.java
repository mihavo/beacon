package io.beacon.locationservice.location.service;

import io.beacon.locationservice.authz.FriendshipPermissionService;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.grpc.clients.UserGrpcClient;
import io.beacon.locationservice.location.fetch.FetchService;
import io.beacon.locationservice.location.geospatial.GeospatialService;
import io.beacon.locationservice.models.UserLocation;
import io.beacon.permissions.FriendshipAction;
import java.util.UUID;
import locationservice.LocationServiceOuterClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

  private final FriendshipPermissionService friendshipPermissionService;
  private final FetchService fetchService;
  private final GeospatialService geospatialService;
  private final UserGrpcClient userGrpcClient;

  public Flux<Location> fetchRecent(UUID userId) {
      return friendshipPermissionService.canPerform(userId, FriendshipAction.VIEW_LOCATION).flatMapMany(canPerform -> {
        if(!canPerform) {
          return Flux.error(new AccessDeniedException("You cannot view this user's location"));
        }
        return  fetchService.fetchRecent(userId); 
      });
    }

  public Flux<UserLocation> fetchLKL(LocationServiceOuterClass.BoundingBox boundingBox, UUID userId) {
    return fetchService.fetchLKL(boundingBox, userId);
  }
}
