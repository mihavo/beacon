package io.beacon.locationservice.location;

import io.beacon.locationservice.authz.FriendshipPermissionService;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.location.fetch.FetchService;
import io.beacon.permissions.FriendshipAction;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final FriendshipPermissionService friendshipPermissionService;
  private final FetchService fetchService;

    public Flux<Location> fetchRecent(UUID userId) {
      return friendshipPermissionService.canPerform(userId, FriendshipAction.VIEW_LOCATION).flatMapMany(canPerform -> {
        if(!canPerform) {
          return Flux.error(new AccessDeniedException("You cannot view this user's location"));
        }
        return  fetchService.fetchRecent(userId); 
      });
    }
}
