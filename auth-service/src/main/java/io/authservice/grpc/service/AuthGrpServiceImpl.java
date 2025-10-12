package io.authservice.grpc.service;

import authservice.AuthServiceGrpc;
import authservice.AuthServiceOuterClass.GetPublicKeyResponse;
import io.authservice.utils.JWTUtility;
import io.grpc.stub.StreamObserver;
import java.security.PublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

  private final JWTUtility jwtUtility;

  public void getPublicKey(StreamObserver<GetPublicKeyResponse> responseObserver) {
    try {
      PublicKey publicKey = jwtUtility.getPublicKey();
      GetPublicKeyResponse response = GetPublicKeyResponse.newBuilder()
          .setKey(publicKey.toString())
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      // If something goes wrong, notify the client
      responseObserver.onError(e);
    }
  }

}
