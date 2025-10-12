package io.beacon.userservice.security;


import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

  @Override
  public Mono<Void> onAuthenticationFailure(WebFilterExchange exchange,
      AuthenticationException exception) {
    ServerWebExchange webExchange = exchange.getExchange();
    webExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    String body = "{\"error\": \"" + exception.getMessage() + "\"}";
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    webExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return webExchange.getResponse().writeWith(Mono.just(
        webExchange.getResponse().bufferFactory().wrap(bytes)
    ));
  }
}