package io.beacon.authservice.controller;

import io.beacon.authservice.dto.LoginRequest;
import io.beacon.authservice.dto.LoginResponse;
import io.beacon.authservice.dto.RegisterRequest;
import io.beacon.authservice.dto.RegisterResponse;
import io.beacon.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public Mono<ResponseEntity<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return authService.register(request).map(response ->
        ResponseEntity.status(HttpStatus.CREATED).body(response)
    ).onErrorResume(e -> {
      if (e instanceof ResponseStatusException) {
        return Mono.just(ResponseEntity.status(((ResponseStatusException) e).getStatusCode())
            .body(new RegisterResponse(null, e.getMessage(), null)));
      }
      return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new RegisterResponse(null, "Registration failed", null)));
    });
  }

  @PostMapping("/login")
  public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request).map(response ->
        ResponseEntity.status(HttpStatus.CREATED).body(response)
    );
  }
}
