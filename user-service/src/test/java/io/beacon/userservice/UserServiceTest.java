package io.beacon.userservice;

import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.beacon.userservice.user.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserServiceTest extends UserServiceApplicationTests {

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  final static String TEST_USER_ID = "fc18ed06-33f5-4513-87ff-0a70129a13b5";

  @Test
  public void shouldRetrieveUser_whenExists() {
    User user = UserTestsUtils.givenUserExists(userRepository, "tester");

    UserResponse response = userService.getUser(user.getId()).block();
    assertEquals(user.getId(), response.id());
    assertEquals(user.getUsername(), response.username());
    assertEquals(user.getFullName(), response.fullName());
  }

  @Test
  public void shouldReturnEmptyResponse_whenRetrievingNonExistentUser() {
    UserResponse response = userService.getUser(UUID.randomUUID()).block();
    assertNull(response);
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void shouldDeleteUser_whenExists() {
    User user = UserTestsUtils.givenUserExists(userRepository, UUID.fromString(TEST_USER_ID));
    userService.deleteCurrentUser().block();
    UserResponse response = userService.getUser(user.getId()).block();
    assertNull(response);
  }
}
