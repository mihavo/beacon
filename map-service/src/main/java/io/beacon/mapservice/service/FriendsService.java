package io.beacon.mapservice.service;

import io.beacon.events.FriendshipEvent;
import io.beacon.mapservice.utils.CacheUtils;
import io.beacon.permissions.FriendshipAction;
import io.beacon.security.utils.AuthUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendsService {

  private final ReactiveStringRedisTemplate stringRedisTemplate;
  private final UserServiceGrpc.UserServiceBlockingStub userStub;

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
      case FRIEND_REMOVED -> stringRedisTemplate.opsForSet().remove(friendshipListKey, friendId).doOnSuccess(
          r -> log.info("Removed friend from cache for user {}", event.userId())).then();
    };
  }

  public Mono<Boolean> canPerform(UUID requesterId, UUID targetUserId, FriendshipAction action) {
    return switch (action) {
      case VIEW_LOCATION -> isInFriendsList(requesterId.toString(), targetUserId.toString());
      //TODO: check for additional actions in the future
    };
  }

  public Mono<Boolean> canPerformSelf(UUID targetUserId, FriendshipAction action) {
    return AuthUtils.getCurrentUserId().flatMap(userId -> canPerform(userId, targetUserId, action));
  }

  private Mono<Boolean> isInFriendsList(String userId, String targetUserId) {
    String key = CacheUtils.getFriendshipListKey(userId);
    return stringRedisTemplate.opsForSet().isMember(key, targetUserId);
  }

  //Forces full re-sync with user service's friendship relationships for each user.
  //TODO: since this cannot scale /adds overhead we need to figure out incremental resync for one user each time
  @Scheduled(fixedDelay = 5 * 60 * 1000)
  public void syncAllFriendships() {
    List<UserServiceOuterClass.Friendship> allFriendships =
        userStub.getAllFriendships(UserServiceOuterClass.GetAllFriendshipsRequest.newBuilder().build()).getFriendshipList();

    allFriendships.forEach((friendship) -> {
      String key = CacheUtils.getFriendshipListKey(friendship.getFirstUserId());
    });
  }
}
