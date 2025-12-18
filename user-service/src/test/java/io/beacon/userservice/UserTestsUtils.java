package io.beacon.userservice;

import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import java.util.Arrays;

public class UserTestsUtils {

  static void givenUserExists(UserRepository repository, String username) {
    repository.save(TestUserDataFactory.createUserWithUsername(username)).block();
  }

  static void givenUsersExist(UserRepository repository, String... usernames) {
    Arrays.stream(usernames).forEach(username -> givenUserExists(repository, username));
  }

  static void givenUserHasFriend(UserRepository repository, User user) {
  }
}
