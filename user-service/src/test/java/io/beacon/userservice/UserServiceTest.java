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
  public void shouldDeleteUser_whenExists() {
    User user = UserTestsUtils.givenUserExists(userRepository, "tester");
    userService.deleteCurrentUser().block();
    UserResponse response = userService.getUser(user.getId()).block();
    assertNull(response);
  }
}
