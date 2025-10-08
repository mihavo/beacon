package io.beacon.userservice.user.service;

import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.user.dto.UserRequest;
import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.mappers.UserMapper;
import io.beacon.userservice.user.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

  public Mono<UserResponse> createUser(UserRequest request) {
    User user = User.builder().username(request.username()).fullName(request.fullName()).build();
    return userRepository.save(user).map(userMapper::toUserResponse);
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
}
