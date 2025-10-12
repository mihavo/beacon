package io.authservice.grpc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass.CreateUserRequest;
import userservice.UserServiceOuterClass.CreateUserResponse;
import userservice.UserServiceOuterClass.GetUserByUsernameRequest;
import userservice.UserServiceOuterClass.GetUserByUsernameResponse;

@Component
@RequiredArgsConstructor
public class UserGrpcClient {

  private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  public String createUser(String username, String fullName, String passwordHash) {
    CreateUserRequest req = CreateUserRequest.newBuilder()
        .setUsername(username)
        .setFullName(fullName)
        .setPasswordHash(passwordHash)
        .build();
    CreateUserResponse res = userServiceStub.createUser(req);
    return res.getId();
  }

  public GetUserByUsernameResponse getUser(String username) {
    GetUserByUsernameRequest req = GetUserByUsernameRequest.newBuilder()
        .setUsername(username)
        .build();
    return userServiceStub.getUserByUsername(req);
  }

}
