package io.beacon.userservice.connections.service;

import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.exceptions.UserNotFoundException;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.mappers.UserMapper;
import io.beacon.userservice.user.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ConnectionsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public Mono<ConnectResponse> connect(UUID targetUserId, UUID userId) {
    Mono<User> targetUser = userRepository.findById(targetUserId).onErrorMap(
        (err) -> new UserNotFoundException("User with id" + targetUserId + " not found"));
    
  }
}
