package io.beacon.userservice;

import io.beacon.userservice.config.UserTestsBase;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@AutoConfigureInProcessTransport
@SpringBootTest
public class UserGrpcServiceTest extends UserTestsBase {

  private static UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  @Autowired
  private UserRepository userRepository;

  @BeforeAll
  static void setup(@Autowired GrpcChannelFactory channelFactory) {
    userServiceStub = UserServiceGrpc.newBlockingStub(channelFactory.createChannel("local"));
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

  @Test
  public void shouldThrowNotFound_whenRetrievingNonExistentUser() {
    String username = "tester";

    UserServiceOuterClass.GetUserByUsernameRequest retrieveRequest = UserServiceOuterClass.GetUserByUsernameRequest.newBuilder()
        .setUsername(username)
        .build();

    StatusRuntimeException exc =
        assertThrows(StatusRuntimeException.class, () -> userServiceStub.getUserByUsername(retrieveRequest));

    assertEquals(Status.NOT_FOUND.getCode(), exc.getStatus().getCode());
    assertTrue(exc.getMessage().contains("User not found"));
  }

  @Test
  public void shouldRetrieveAllFriends_whenUserHasFriends() {
    String username1 = "tester";
    String username2 = "tester2";
    User[] users = UserTestsUtils.givenUsersExist(userRepository, username1, username2);
    UserTestsUtils.givenUserHasFriend(userRepository, users[0], users[1]);

    String userId = users[0].getId().toString();
    UserServiceOuterClass.GetUserFriendsResponse friendships =
        userServiceStub.getUserFriends(
            UserServiceOuterClass.GetUserFriendsRequest.newBuilder().setUserId(userId).build());
    assertEquals(1, friendships.getFriendsCount());
    assertEquals(users[1].getId().toString(), friendships.getFriends(0).getUserId());
    assertEquals(users[1].getUsername(), friendships.getFriends(0).getUsername());
  }

  @Test
  public void shouldRetrieveNoFriends_whenUserHasNoFriends() {
    User user = UserTestsUtils.givenUserExists(userRepository, "tester");

    UserServiceOuterClass.GetUserFriendsResponse friendships =
        userServiceStub.getUserFriends(
            UserServiceOuterClass.GetUserFriendsRequest.newBuilder().setUserId(user.getId().toString()).build());
    assertTrue(friendships.getFriendsList().isEmpty());
  }

  @Test
  public void shouldRetrieveAllFriends() {
    String username1 = "tester";
    String username2 = "tester2";
    String username3 = "tester3";
    User[] users = UserTestsUtils.givenUsersExist(userRepository, username1, username2, username3);
    UserTestsUtils.givenUserHasFriend(userRepository, users[0], users[1]);
    UserTestsUtils.givenUserHasFriend(userRepository, users[0], users[2]);

    String userId = users[0].getId().toString();
    UserServiceOuterClass.GetUserFriendsResponse friendships =
        userServiceStub.getUserFriends(
            UserServiceOuterClass.GetUserFriendsRequest.newBuilder().setUserId(userId).build());
    assertEquals(2, friendships.getFriendsCount());
    List<String> friendIds = friendships.getFriendsList().stream().map(UserServiceOuterClass.User::getUserId).toList();
    assertTrue(friendIds.contains(users[1].getId().toString()));
    assertTrue(friendIds.contains(users[2].getId().toString()));
  }
}
