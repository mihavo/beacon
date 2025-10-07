package io.beacon.userservice.controller;

import io.beacon.userservice.dto.UserRequest;
import io.beacon.userservice.dto.UserResponse;
import io.beacon.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping("/")
  public Mono<ResponseEntity<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
    Mono<UserResponse> user = userService.createUser(request);
    return user.map((response) -> new ResponseEntity<>(response, HttpStatus.CREATED));
  }
}
