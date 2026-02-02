package io.beacon;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockBeaconUserSecurityContextFactory.class)
public @interface WithMockBeaconUser {

  String id();
}