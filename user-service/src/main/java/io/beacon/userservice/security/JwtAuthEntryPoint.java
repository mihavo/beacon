package io.beacon.userservice.security;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthEntryPoint implements ServerAuthenticationEntryPoint {

  @Override
  public Mono<Void> commence(ServerWebExchange exchange,
      org.springframework.security.core.AuthenticationException ex) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

    String body = "{\"error\":\"" + ex.getMessage() + "\"}";
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
        .bufferFactory()
        .wrap(bytes)));
  }
}


