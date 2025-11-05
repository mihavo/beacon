package io.beacon.authservice.service;


import io.beacon.authservice.dto.LoginRequest;
import io.beacon.authservice.dto.LoginResponse;
import io.beacon.authservice.dto.RegisterRequest;
import io.beacon.authservice.dto.RegisterResponse;
import io.beacon.authservice.grpc.clients.UserGrpcClient;
import io.beacon.authservice.utils.JWTUtility;
import io.grpc.StatusRuntimeException;
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
      if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
      }
      String token = jwtUtility.generateToken(user.getId());
      return new LoginResponse("Logged in.", token);
    }).subscribeOn(Schedulers.boundedElastic()).onErrorMap(StatusRuntimeException.class, e -> {
      if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    });
  }

}
