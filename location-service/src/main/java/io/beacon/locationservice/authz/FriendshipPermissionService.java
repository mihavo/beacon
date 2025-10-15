package io.beacon.locationservice.authz;

import io.beacon.events.FriendshipEvent;
import io.beacon.locationservice.utils.CacheUtils;
import io.beacon.permissions.FriendshipAction;
import io.beacon.security.utils.AuthUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipPermissionService {

  private final ReactiveStringRedisTemplate stringRedisTemplate;

  public Mono<Void> handleFriendshipEvent(FriendshipEvent event) {
    String friendshipListKey = CacheUtils.getFriendshipListKey(event.userId());
    String friendId = event.friendId();

    if (event.userId() == null || event.friendId() == null) {
      log.warn("Ignoring malformed event: {}", event);
      return Mono.empty();
    }

    return switch (event.type()) {
      case FRIEND_ADDED -> stringRedisTemplate.opsForSet().add(friendshipListKey, friendId)
          .doOnSuccess(
              r -> log.info("Added new friend to cache for user {}", event.userId())).then();
      case FRIEND_REMOVED ->
          stringRedisTemplate.opsForSet().remove(friendshipListKey, friendId).doOnSuccess(
              r -> log.info("Removed friend from cache for user {}", event.userId())).then();
    };

  }

  public Mono<Boolean> canPerform(UUID targetUserId, FriendshipAction action) {
    Mono<UUID> currentUserId = AuthUtils.getCurrentUserId();
    return switch (action) {
      case VIEW_LOCATION -> currentUserId.flatMap(
          userId -> isInFriendsList(userId.toString(), targetUserId.toString()));
      //TODO: check for additional actions in the future
    };
  }

  private Mono<Boolean> isInFriendsList(String userId, String targetUserId) {
    String key = CacheUtils.getFriendshipListKey(userId);
    return stringRedisTemplate.opsForSet().isMember(key, targetUserId);
  }
}
