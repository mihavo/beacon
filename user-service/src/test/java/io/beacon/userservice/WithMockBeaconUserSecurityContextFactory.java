package io.beacon.userservice;

import io.beacon.security.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockBeaconUserSecurityContextFactory implements WithSecurityContextFactory<WithMockBeaconUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockBeaconUser customUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication auth = new JwtAuthenticationToken(null, customUser.id());
    context.setAuthentication(auth);
    return context;
  }
}