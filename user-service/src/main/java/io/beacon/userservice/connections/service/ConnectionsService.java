package io.beacon.userservice.connections.service;

import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.exceptions.AlreadyFriendsException;
import io.beacon.userservice.exceptions.ConnectionRequestExistsException;
import io.beacon.userservice.exceptions.UserNotFoundException;
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

    if (userId.equals(targetUserId)) {
      return Mono.error(new IllegalArgumentException("Cannot send request to yourself"));
    }

    return userRepository.findById(targetUserId).switchIfEmpty(
            Mono.error(new UserNotFoundException("User with id " + userId + " not found.")))
        .flatMap(user -> performRequestValidations(userId, targetUserId))
        .then(Mono.defer(() -> userRepository.sendFriendRequest(userId, targetUserId).flatMap(
            created -> created ? Mono.just(
                new ConnectResponse("Connect request sent!", Instant.now()))
                : Mono.error(new IllegalStateException("Failed to send connection request.")))));
  }

  private Mono<Void> performRequestValidations(UUID userId, UUID targetUserId) {
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
            "There's a pending request for user" + userId + " already."));
      }
      return Mono.empty();
    });
  }
}
