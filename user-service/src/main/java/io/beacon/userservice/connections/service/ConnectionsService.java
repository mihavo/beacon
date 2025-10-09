package io.beacon.userservice.connections.service;

import io.beacon.userservice.connections.dto.AcceptResponse;
import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.connections.dto.DeclineResponse;
import io.beacon.userservice.connections.dto.RemoveConnectionResponse;
import io.beacon.userservice.exceptions.AlreadyFriendsException;
import io.beacon.userservice.exceptions.ConnectionRequestExistsException;
import io.beacon.userservice.exceptions.ConnectionRequestNotExistsException;
import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ConnectionsService {

  private final UserRepository userRepository;

  public Mono<ConnectResponse> connect(UUID targetUserId, UUID userId) {

    return isSameUser(targetUserId, userId).then(
        Mono.defer(() -> checkUserExistence(targetUserId, userId)
            .flatMap(user -> performConnectRequestValidations(userId, targetUserId))
            .then(Mono.defer(() -> userRepository.sendFriendRequest(userId, targetUserId).flatMap(
                created -> created ? Mono.just(
                    new ConnectResponse("Connect request sent!", Instant.now()))
                    : Mono.error(
                        new IllegalStateException("Failed to send connection request")))))));
  }

  public Mono<AcceptResponse> accept(UUID targetUserId, UUID userId) {

    return isSameUser(targetUserId, userId).then(
        Mono.defer(() -> checkUserExistence(targetUserId, userId)
            .flatMap(user -> performAcceptRequestValidations(userId, targetUserId))
            .then(Mono.defer(() -> userRepository.acceptFriendRequest(userId, targetUserId)))
            .flatMap(
                created -> created ? Mono.just(
                    new AcceptResponse("You are now connected with user: " + targetUserId + "!"))
                    : Mono.error(new IllegalArgumentException("Failed to accept request")))));
  }

  public Mono<DeclineResponse> decline(UUID targetUserId, UUID userId) {
    return isSameUser(targetUserId, userId).then(
            Mono.defer(() -> checkUserExistence(targetUserId, userId)))
        .flatMap(user -> performDeclineRequestValidations(userId, targetUserId))
        .then(Mono.defer(() -> userRepository.deleteRequest(userId, targetUserId))).flatMap(
            deleted -> deleted ? Mono.empty()
                : Mono.error(new IllegalArgumentException("Could not delete connection request")));
  }

  public Mono<RemoveConnectionResponse> remove(UUID targetUserId, UUID userId) {
    return isSameUser(targetUserId, userId).then(
            Mono.defer(() -> checkUserExistence(targetUserId, userId)))
        .flatMap(user -> performRemoveRequestValidations(userId, targetUserId))
        .then(Mono.defer(() -> userRepository.removeFriend(userId, targetUserId))).flatMap(
            deleted -> deleted ? Mono.empty()
                : Mono.error(new IllegalArgumentException("Could not remove connection")));
  }


  private Mono<Void> performDeclineRequestValidations(UUID userId, UUID targetUserId) {
    return Mono.zip(
        userRepository.areFriends(userId, targetUserId),
        userRepository.hasPendingRequest(userId, targetUserId)
    ).flatMap(validationResults -> {
      Boolean areFriends = validationResults.getT1();
      Boolean hasPendingRequest = validationResults.getT2();

      if (areFriends || !hasPendingRequest) {
        return Mono.error(new ConnectionRequestNotExistsException(
            "There is no existing connection request with user: " + targetUserId));
      }
      return Mono.empty();
    });
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
            new AlreadyFriendsException("You are not connected with user: " + userId));
      }
      return Mono.empty();
    });
  }


  private Mono<Void> performAcceptRequestValidations(UUID userId, UUID targetUserId) {
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
      ;
      if (!pendingRequestExists) {
        return Mono.error(new ConnectionRequestNotExistsException(
            "There is no pending request for user: " + userId));
      }
      return Mono.empty();
    });
  }


  private Mono<Void> isSameUser(UUID targetUserId, UUID userId) {
    if (userId.equals(targetUserId)) {
      return Mono.error(new IllegalArgumentException("Cannot send request to yourself"));
    }
    return Mono.empty();
  }


  private Mono<User> checkUserExistence(UUID targetUserId, UUID userId) {
    return userRepository.findById(targetUserId).switchIfEmpty(
        Mono.error(new UserNotFoundException("User with id " + userId + " not found.")));
  }
}
