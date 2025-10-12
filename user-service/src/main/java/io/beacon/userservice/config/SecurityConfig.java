package io.beacon.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
    return http.authorizeExchange(exchanges -> exchanges
            .pathMatchers("/healthcheck/**").permitAll().anyExchange().denyAll())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .build();
  }
}

