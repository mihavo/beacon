package io.beacon.userservice;

import io.beacon.WithMockBeaconUser;
import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.beacon.userservice.user.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static io.beacon.userservice.UserTestsUtils.TEST_USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserServiceTest extends UserServiceApplicationTests {

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  @BeforeEach
  public void setUp() {
    userRepository.deleteAll().block();
  }

  @Test
  public void retrieve_shouldRetrieveUser_whenExists() {
    User user = UserTestsUtils.givenUserExists(userRepository, "tester");

    UserResponse response = userService.getUser(user.getId()).block();
    assertEquals(user.getId(), response.id());
    assertEquals(user.getUsername(), response.username());
    assertEquals(user.getFullName(), response.fullName());
  }

  @Test
  public void retrieve_shouldReturnEmptyResponse_whenRetrievingNonExistentUser() {
    UserResponse response = userService.getUser(UUID.randomUUID()).block();
    assertNull(response);
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void delete_shouldDeleteUser_whenExists() {
    User user = UserTestsUtils.givenUserExists(userRepository, UUID.fromString(TEST_USER_ID));
    userService.deleteCurrentUser().block();
    UserResponse response = userService.getUser(user.getId()).block();
    assertNull(response);
  }

  @Test
  public void search_shouldReturnUser_whenUsernameContainsQuery() {
    UserTestsUtils.givenUserExists(userRepository, "tester");
    StepVerifier.create(userService.search("st")).expectNextMatches(user -> user.username().equals("tester")).verifyComplete();
  }

  @Test
  public void search_shouldBeCaseInsensitive_whenUsernameContainsQuery() {
    UserTestsUtils.givenUserExists(userRepository, "Tester");

    StepVerifier.create(userService.search("TEST"))
        .expectNextCount(1)
        .verifyComplete();
  }

  @Test
  public void search_shouldReturnEmpty_whenNoUsernameMatchesQuery() {
    UserTestsUtils.givenUsersExist(userRepository, "tester", "user", "admin");
    StepVerifier.create(userService.search("ran")).verifyComplete();
  }
}
