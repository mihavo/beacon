package io.beacon.notificationservice.config;

import io.beacon.security.config.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableWebFluxSecurity
@Import(SecurityAutoConfiguration.class)
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
      AuthenticationWebFilter filter, ServerAuthenticationEntryPoint entryPoint) {
    return http.authorizeExchange(exchanges -> exchanges
            .pathMatchers("/actuator/**").permitAll().anyExchange().authenticated())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling(
            exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint(entryPoint))
        .build();
  }
}

