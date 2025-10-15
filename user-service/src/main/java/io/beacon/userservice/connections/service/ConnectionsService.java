package io.beacon.userservice.connections.service;

import io.beacon.events.FriendshipEvent;
import io.beacon.events.enums.FriendshipEventType;
import io.beacon.userservice.connections.dto.AcceptResponse;
import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.connections.dto.ConnectionsInfo;
import io.beacon.userservice.connections.dto.DeclineResponse;
import io.beacon.userservice.connections.dto.RemoveConnectionResponse;
import io.beacon.userservice.connections.dto.UserStatusInfo;
import io.beacon.userservice.events.FriendshipEventProducer;
import io.beacon.userservice.exceptions.AlreadyFriendsException;
import io.beacon.userservice.exceptions.ConnectionRequestExistsException;
import io.beacon.userservice.exceptions.ConnectionRequestNotExistsException;
import io.beacon.userservice.exceptions.SelfConnectRequestException;
import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.beacon.userservice.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ConnectionsService {

  private final UserRepository userRepository;
  private final UserService userService;
  private final FriendshipEventProducer friendshipEventProducer;

  public Mono<ConnectResponse> connect(UUID targetUserId) {
    return userService.getCurrentUserId().flatMap(userId ->
        isSameUser(targetUserId, userId).then(
            Mono.defer(() -> checkUserExistence(targetUserId)
                .flatMap(user -> performConnectRequestValidations(userId, targetUserId))
                .then(
                    Mono.defer(() -> userRepository.sendFriendRequest(userId, targetUserId).flatMap(
                        created -> created ? Mono.just(
                            new ConnectResponse("Connect request sent!", Instant.now()))
                            : Mono.error(
                                new IllegalStateException(
                                    "Failed to send connection request"))))))));
  }

  public Mono<AcceptResponse> accept(UUID targetUserId) {
    return userService.getCurrentUserId().flatMap(userId -> isSameUser(targetUserId, userId).then(
        Mono.defer(() -> checkUserExistence(targetUserId)
            .flatMap(user -> performAcceptRequestValidations(targetUserId, userId))
            .then(Mono.defer(() -> userRepository.acceptFriendRequest(targetUserId, userId)))
            .flatMap(created -> {
              if (!created) {
                return Mono.error(new IllegalArgumentException("Failed to accept request"));
              }
              AcceptResponse response = new AcceptResponse(
                  "You are now connected with user: " + targetUserId + "!"
              );
              return performPostAcceptOperations(userId, targetUserId).thenReturn(response);
            })
        )));
  }

  private Mono<Void> performPostAcceptOperations(UUID senderId, UUID receiverId) {
    return friendshipEventProducer.send(
        new FriendshipEvent(FriendshipEventType.FRIEND_ADDED, receiverId.toString(),
            senderId.toString(), Instant.now()));
  }

  public Mono<DeclineResponse> decline(UUID targetUserId) {
    return userService.getCurrentUserId().flatMap(userId ->
        isSameUser(targetUserId, userId).then(
                Mono.defer(() -> checkUserExistence(targetUserId)))
            .flatMap(user -> performDeclineRequestValidations(userId))
            .then(Mono.defer(() -> userRepository.deleteRequest(userId, targetUserId))).flatMap(
                deleted -> deleted ? Mono.empty()
                    : Mono.error(new IllegalArgumentException("Could not decline friend request"))));
  }

  public Mono<RemoveConnectionResponse> removeFriend(UUID targetUserId) {
    return userService.getCurrentUserId().flatMap(userId ->
        isSameUser(targetUserId, userId).then(
                Mono.defer(() -> checkUserExistence(targetUserId)))
            .flatMap(user -> performRemoveRequestValidations(userId, targetUserId))
            .then(Mono.defer(() -> userRepository.removeFriend(userId, targetUserId))).flatMap(
                deleted -> deleted ? Mono.empty()
                    : Mono.error(new IllegalArgumentException("Could not remove friend"))));
  }

  private Mono<Void> performDeclineRequestValidations(UUID targetUserId) {
    return userService.getCurrentUserId().flatMap(userId ->
        isSameUser(targetUserId, userId).then(Mono.zip(
            userRepository.areFriends(userId, targetUserId),
            userRepository.hasPendingRequest(userId, targetUserId)
        )).flatMap(validationResults -> {
          Boolean areFriends = validationResults.getT1();
          Boolean hasPendingRequest = validationResults.getT2();

          if (areFriends || !hasPendingRequest) {
            return Mono.error(new ConnectionRequestNotExistsException(
                "There is no existing friend request with user: " + targetUserId));
          }
          return Mono.empty();
        }));
  }

  private Mono<Void> performConnectRequestValidations(UUID userId, UUID targetUserId) {
    return Mono.zip(
        userRepository.areFriends(userId, targetUserId),
        userRepository.hasPendingRequest(userId, targetUserId)
    ).flatMap(validationResults -> {
      Boolean areFriends = validationResults.getT1();
      Boolean pendingRequestExists = validationResults.getT2();

      if (areFriends) {
        return Mono.error(
            new AlreadyFriendsException("You are already friends with user: " + userId));
      }
      if (pendingRequestExists) {
        return Mono.error(new ConnectionRequestExistsException(
            "There exists a pending request for user: " + userId + " already."));
      }
      return Mono.empty();
    });
  }

  private Mono<Void> performRemoveRequestValidations(UUID userId, UUID targetUserId) {
    return userRepository.areFriends(userId, targetUserId).flatMap(areFriends -> {
      if (!areFriends) {
        return Mono.error(
            new AlreadyFriendsException("You are not friends with user: " + userId));
      }
      return Mono.empty();
    });
  }

  private Mono<Void> performAcceptRequestValidations(UUID senderId, UUID receiverId) {
    return Mono.zip(
        userRepository.areFriends(senderId, receiverId),
        userRepository.hasPendingRequest(senderId, receiverId)
    ).flatMap(validationResults -> {
      Boolean areFriends = validationResults.getT1();
      Boolean pendingRequestExists = validationResults.getT2();

      if (areFriends) {
        return Mono.error(
            new AlreadyFriendsException("You are already friends with user: " + senderId));
      }
      ;
      if (!pendingRequestExists) {
        return Mono.error(new ConnectionRequestNotExistsException(
            "There is no pending request for user: " + receiverId));
      }
      return Mono.empty();
    });
  }

  private Mono<Void> isSameUser(UUID targetUserId, UUID userId) {
    if (userId.equals(targetUserId)) {
      return Mono.error(new SelfConnectRequestException("Cannot send request to yourself"));
    }
    return Mono.empty();
  }

  private Mono<User> checkUserExistence(UUID userId) {
    return userRepository.findById(userId).switchIfEmpty(
        Mono.error(new UserNotFoundException("User with id " + userId + " not found.")));
  }

  public Mono<UserStatusInfo> getStatus(UUID targetUserId) {
    return userService.getCurrentUserId()
        .flatMap(userId -> userRepository.getRelationshipType(userId, targetUserId))
        .map(UserStatusInfo::new);
  }

  public Mono<ConnectionsInfo> getConnections() {
    return userService.getCurrentUserId().flatMap(
        userId -> userRepository.getConnections(userId).collectList().map(ConnectionsInfo::new));
  }

  public Mono<ConnectionsInfo> getFriends() {
    return userService.getCurrentUserId().flatMap(
        userId -> userRepository.getFriends(userId).collectList().map(ConnectionsInfo::new));
  }
}
