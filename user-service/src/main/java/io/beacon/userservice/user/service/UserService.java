package io.beacon.userservice.user.service;

import io.beacon.security.utils.AuthUtils;
import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.mappers.UserMapper;
import io.beacon.userservice.user.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

  private final UserMapper userMapper;

  private final UserRepository userRepository;

  public Mono<UserResponse> getUser(UUID userId) {
    return userRepository.findById(userId).map(userMapper::toUserResponse);
  }

  public Mono<UserResponse> getSelf() {
    return AuthUtils.getCurrentUserId().flatMap(userId -> userRepository.findById(userId).map(userMapper::toUserResponse));
  }

  public Mono<Void> deleteUser(UUID userId) {
    return userRepository.existsById(userId)
        .flatMap(exists -> {
          if (!exists) {
            return Mono.error(new UserNotFoundException("User with id " + userId + " not found"));
          }
          return userRepository.deleteById(userId);
        });
  }

  public Mono<UUID> getCurrentUserId() {
    return
        ReactiveSecurityContextHolder.getContext().flatMap(context -> Mono.just(UUID.fromString(
                (String) context.getAuthentication().getPrincipal())))
        .switchIfEmpty(Mono.error(
            new AuthenticationCredentialsNotFoundException("No user is currently authenticated")));
  }

  public Flux<UserResponse> search(String query) {
    return userRepository.fullTextSearch(query).map(userMapper::toUserResponse);
  }
}
