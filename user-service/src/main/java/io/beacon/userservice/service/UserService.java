package io.beacon.userservice.service;

import io.beacon.userservice.dto.UserRequest;
import io.beacon.userservice.dto.UserResponse;
import io.beacon.userservice.entity.User;
import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.mappers.UserMapper;
import io.beacon.userservice.repository.UsersRepository;
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

  private final UsersRepository usersRepository;

  public Mono<UserResponse> getUser(UUID userId) {
    return usersRepository.findById(userId).map(userMapper::toUserResponse);
  }

  public Mono<UserResponse> createUser(UserRequest request) {
    User user = User.builder().username(request.username()).fullName(request.fullName()).build();
    return usersRepository.save(user).map(userMapper::toUserResponse);
  }

  public Mono<Void> deleteUser(UUID userId) {
    return usersRepository.existsById(userId)
        .flatMap(exists -> {
          if (!exists) {
            return Mono.error(new UserNotFoundException("User with id " + userId + " not found"));
          }
          return usersRepository.deleteById(userId);
        });
  }
}
