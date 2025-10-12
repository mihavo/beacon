package io.beacon.userservice.security;

import io.beacon.userservice.grpc.clients.AuthGrpcClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
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
    if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
      return Mono.error(
          new AuthenticationCredentialsNotFoundException("Unsupported authentication type"));
    }
    String token = authentication.getCredentials().toString();
    try {
      Jws<Claims> claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
      JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(token,
          claims.getPayload().getSubject());
      authenticationToken.setAuthenticated(true);
      return Mono.just(authenticationToken);
    } catch (ExpiredJwtException e) {
      return Mono.error(new CredentialsExpiredException("JWT token expired", e));
    } catch (JwtException e) {
      return Mono.error(new BadCredentialsException("Invalid JWT token", e));
    }
  }


}
