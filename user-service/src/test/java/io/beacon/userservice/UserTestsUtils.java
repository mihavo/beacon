package io.beacon.userservice;

import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import java.util.Arrays;

public class UserTestsUtils {

  static User givenUserExists(UserRepository repository, String username) {
    return repository.save(TestUserDataFactory.createUserWithUsername(username)).block();
  }

  static User[] givenUsersExist(UserRepository repository, String... usernames) {
    return Arrays.stream(usernames).map(username -> givenUserExists(repository, username)).toArray(User[]::new);
  }

  static void givenUserHasFriend(UserRepository repository, User selfUser, User targetUser) {
    repository.createFriend(selfUser.getId(), targetUser.getId()).block();
  }
}
