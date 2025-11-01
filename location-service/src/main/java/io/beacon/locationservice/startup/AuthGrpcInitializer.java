package io.beacon.locationservice.startup;

import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.openssl.PEMException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthGrpcInitializer {

  private final AuthGrpcClient authGrpcClient;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() throws PEMException {
    authGrpcClient.load();
  }
}