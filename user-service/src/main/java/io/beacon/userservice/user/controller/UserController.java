package io.beacon.userservice.user.controller;

import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.service.UserService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public Mono<ResponseEntity<UserResponse>> getUser(@PathVariable UUID userId) {
    Mono<UserResponse> user = userService.getUser(userId);
    return user.map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("/me")
  public Mono<ResponseEntity<UserResponse>> getSelf() {
    Mono<UserResponse> self = userService.getSelf();
    return self.map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{userId}")
  public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID userId) {
    return userService.deleteUser(userId).thenReturn(ResponseEntity.ok().build());
  }
}
