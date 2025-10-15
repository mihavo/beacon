package io.beacon.locationservice.grpc.clients;

import authservice.AuthServiceGrpc.AuthServiceBlockingStub;
import authservice.AuthServiceOuterClass.GetPublicKeyRequest;
import io.beacon.security.providers.PublicKeyProvider;
import jakarta.annotation.PostConstruct;
import java.security.PublicKey;
import java.util.Base64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Component;

@Component
@Getter
@RequiredArgsConstructor
public class AuthGrpcClient implements PublicKeyProvider {

  private final AuthServiceBlockingStub authServiceStub;
  private PublicKey publicKey;

  @PostConstruct
  public void load() throws PEMException {
    String receivedKey = authServiceStub.getPublicKey(
        GetPublicKeyRequest.newBuilder().build()).getKey();
    byte[] decoded = Base64.getDecoder().decode(receivedKey);
    publicKey = new JcaPEMKeyConverter().getPublicKey(SubjectPublicKeyInfo.getInstance(decoded));
  }
}
