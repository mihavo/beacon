package io.beacon.locationservice.location.service;

import io.beacon.locationservice.authz.FriendshipPermissionService;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.grpc.clients.UserGrpcClient;
import io.beacon.locationservice.location.fetch.FetchService;
import io.beacon.locationservice.location.geospatial.GeospatialService;
import io.beacon.locationservice.models.UserLocation;
import io.beacon.locationservice.stream.StreamService;
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
  private final StreamService streamService;

  /**
   * Allows users to fetch recent locations of a specified user id
   *
   * @param userId the user's id of which to fetch the locations from
   * @return a flux of the recent location objects of the user
   */
  public Flux<Location> fetchRecent(UUID userId) {
      return friendshipPermissionService.canPerform(userId, FriendshipAction.VIEW_LOCATION).flatMapMany(canPerform -> {
        if(!canPerform) {
          return Flux.error(new AccessDeniedException("You cannot view this user's location"));
        }
        return  fetchService.fetchRecent(userId); 
      });
    }

  /**
   * Fetches all the last known locations of a provided user's inside a bounding box
   *
   * @param boundingBox the bounding box to search in
   * @param userId      the user's id of which the friends' locations will be fetched
   * @return flux of recent user locations (location + user id)
   */
  public Flux<UserLocation> fetchFriendsLKL(LocationServiceOuterClass.BoundingBox boundingBox, UUID userId) {
    return fetchService.fetchFriendsLKL(boundingBox, userId);
  }

  /**
   * Streams the locations of a provided user, given that there's a friendship relation
   * with the currently authenticated user
   *
   * @param userId the user of which the locations will be streamed
   * @return the flux of real-time locations
   */
  public Flux<Location> streamLocations(UUID userId) {
    return friendshipPermissionService.canPerform(userId, FriendshipAction.VIEW_LOCATION).flatMapMany(canView -> {
      if (!canView) {
        return Flux.error(new AccessDeniedException("You cannot view this user's location"));
      }
      return streamService.stream(userId);
    });
  }
}
