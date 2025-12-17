package io.beacon.userservice;

import io.beacon.userservice.user.entity.User;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.Test;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

public class UserGrpcServiceTest extends UserServiceApplicationTests {

  @GrpcClient("userService")
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  @Test
  public void shouldCreateAndRetrieveUser_whenNotExists() {
    User user = TestUserDataFactory.createRandomUser();
    UserServiceOuterClass.CreateUserRequest createRequest = UserServiceOuterClass.CreateUserRequest.newBuilder()
        .setUsername(user.getUsername())
        .setFullName(user.getFullName())
        .setPasswordHash(user.getPassword())
        .build();

    UserServiceOuterClass.CreateUserResponse userResponse = userServiceStub.createUser(createRequest);
  }
}
