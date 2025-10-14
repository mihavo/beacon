package io.beacon.authservice.grpc.service;

import authservice.AuthServiceGrpc;
import authservice.AuthServiceOuterClass.GetPublicKeyRequest;
import authservice.AuthServiceOuterClass.GetPublicKeyResponse;
import io.beacon.authservice.utils.JWTUtility;
import io.grpc.stub.StreamObserver;
import java.security.PublicKey;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

  private final JWTUtility jwtUtility;
  
  @Override
  public void getPublicKey(GetPublicKeyRequest request,
      StreamObserver<GetPublicKeyResponse> responseObserver) {
    try {
      PublicKey publicKey = jwtUtility.getPublicKey();
      String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
      GetPublicKeyResponse response = GetPublicKeyResponse.newBuilder()
          .setKey(encodedKey)
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      // If something goes wrong, notify the client
      responseObserver.onError(e);
    }
  }

}
