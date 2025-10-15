package io.beacon.security.config;

import io.beacon.security.jwt.JwtAuthEntryPoint;
import io.beacon.security.jwt.JwtAuthManager;
import io.beacon.security.jwt.JwtAuthenticationFailureHandler;
import io.beacon.security.jwt.JwtAuthenticationToken;
import io.beacon.security.providers.PublicKeyProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityAutoConfiguration {

  @Bean
  // Creates the jwt auth manager only if a public key provider exists
  @ConditionalOnBean(PublicKeyProvider.class)
  public ReactiveAuthenticationManager jwtAuthManager(PublicKeyProvider keyProvider) {
    return new JwtAuthManager(keyProvider);
  }

  @Bean
  @ConditionalOnMissingBean(AuthenticationWebFilter.class)
  public AuthenticationWebFilter jwtAuthenticationFilter(
      ReactiveAuthenticationManager authManager) {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(authManager);
    filter.setServerAuthenticationConverter(exchange -> {
      String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        return Mono.just(new JwtAuthenticationToken(token, null));
      }
      return Mono.empty();
    });
    filter.setAuthenticationFailureHandler(new JwtAuthenticationFailureHandler());
    return filter;
  }

  @Bean
  @ConditionalOnMissingBean(ServerAuthenticationEntryPoint.class)
  public ServerAuthenticationEntryPoint jwtEntryPoint() {
    return new JwtAuthEntryPoint();
  }

}
