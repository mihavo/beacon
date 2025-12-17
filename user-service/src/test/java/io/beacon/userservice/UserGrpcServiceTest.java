package io.beacon.userservice;

import io.beacon.userservice.user.entity.User;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

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

    userServiceStub.createUser(createRequest);

    UserServiceOuterClass.GetUserByUsernameRequest retrieveRequest = UserServiceOuterClass.GetUserByUsernameRequest.newBuilder()
        .setUsername(createRequest.getUsername())
        .build();

    UserServiceOuterClass.GetUserByUsernameResponse retrievalResponse = userServiceStub.getUserByUsername(retrieveRequest);

    assertNotNull(retrievalResponse.getId());
    assertEquals(createRequest.getUsername(), retrievalResponse.getUsername());
    assertEquals(createRequest.getFullName(), retrievalResponse.getFullName());
    assertEquals(createRequest.getPasswordHash(), retrievalResponse.getPasswordHash());
  }
}
