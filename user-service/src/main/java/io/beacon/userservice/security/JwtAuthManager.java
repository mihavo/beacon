package io.beacon.userservice.security;

import io.beacon.userservice.grpc.clients.AuthGrpcClient;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthManager implements
    ReactiveAuthenticationManager {

  private final PublicKey publicKey;

  public JwtAuthManager(AuthGrpcClient authGrpcClient) {
    this.publicKey = authGrpcClient.getPublicKey();
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String token = authentication.getCredentials().toString();

    try {
      Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
    } catch (JwtException e) {
      return Mono.empty();
    }
    return Mono.just(new UsernamePasswordAuthenticationToken("user", token,
        java.util.Collections.emptyList()));
  }
}
