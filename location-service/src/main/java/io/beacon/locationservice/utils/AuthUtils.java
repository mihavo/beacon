package io.beacon.locationservice.utils;

import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@UtilityClass
public class AuthUtils {

  public Mono<UUID> getCurrentUserId() {
    return
        ReactiveSecurityContextHolder.getContext().flatMap(context -> Mono.just(UUID.fromString(
                (String) context.getAuthentication().getPrincipal())))
            .switchIfEmpty(Mono.error(
                new AuthenticationCredentialsNotFoundException(
                    "No user is currently authenticated")));
  }
}
