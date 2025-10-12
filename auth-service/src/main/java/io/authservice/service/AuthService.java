package io.authservice.service;


import io.authservice.dto.LoginRequest;
import io.authservice.dto.LoginResponse;
import io.authservice.dto.RegisterRequest;
import io.authservice.dto.RegisterResponse;
import io.authservice.grpc.clients.UserGrpcClient;
import io.authservice.utils.JWTUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import userservice.UserServiceOuterClass.GetUserByUsernameResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserGrpcClient userGrpcClient;
  private final PasswordEncoder passwordEncoder;
  private final JWTUtility jwtUtility;

  public Mono<RegisterResponse> register(RegisterRequest request) {
    return Mono.fromCallable(() -> {
          String passwordHash = passwordEncoder.encode(request.password());
          String userId = userGrpcClient.createUser(request.username(), request.fullName(),
              passwordHash);
          String token = jwtUtility.generateToken(userId);
          return new RegisterResponse(userId, "User created successfully.", token);
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<LoginResponse> login(LoginRequest request) {
    return Mono.fromCallable(() -> {
      GetUserByUsernameResponse user = userGrpcClient.getUser(request.username());
      if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
      }
      String token = jwtUtility.generateToken(request.username());
      return new LoginResponse("Logged in.", token);
    }).subscribeOn(Schedulers.boundedElastic());
  }

}
