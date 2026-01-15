package io.beacon.locationservice.config;

import io.beacon.security.jwt.JwtAuthenticationToken;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import reactor.core.publisher.Mono;

import static io.beacon.TestUserConstants.TEST_USER_ID;

@TestConfiguration
public class NoAuthSecurityConfig {

  @Bean
  @Primary
  public ReactiveAuthenticationManager reactiveAuthenticationManager() {
    return authentication -> {
      JwtAuthenticationToken authToken = new JwtAuthenticationToken(
          "test-token",
          TEST_USER_ID
      );
      authToken.setAuthenticated(true);
      return Mono.just(authToken);
    };
  }
}
