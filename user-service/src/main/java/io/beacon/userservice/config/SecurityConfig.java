package io.beacon.userservice.config;

import io.beacon.userservice.security.JwtAuthEntryPoint;
import io.beacon.userservice.security.JwtAuthManager;
import io.beacon.userservice.security.JwtAuthenticationFailureHandler;
import io.beacon.userservice.security.JwtAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
      JwtAuthManager jwtAuthManager) {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtAuthManager);
    filter.setServerAuthenticationConverter(exchange -> {
      String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        return Mono.just(new JwtAuthenticationToken(token, null));
      }
      return Mono.empty();
    });
    filter.setAuthenticationFailureHandler(new JwtAuthenticationFailureHandler());

    return http.authorizeExchange(exchanges -> exchanges
            .pathMatchers("/healthcheck/**").permitAll().anyExchange().denyAll())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint(
            new JwtAuthEntryPoint()))
        .build();
  }

}

