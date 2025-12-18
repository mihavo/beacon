package io.beacon.userservice;

import io.beacon.userservice.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@AutoConfigureInProcessTransport
public class UserGrpcServiceTest extends UserServiceApplicationTests {

  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  @BeforeEach
  void setup(@Autowired GrpcChannelFactory channelFactory) {
    userServiceStub = UserServiceGrpc.newBlockingStub(channelFactory.createChannel("0.0.0.0:0"));
  }

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
