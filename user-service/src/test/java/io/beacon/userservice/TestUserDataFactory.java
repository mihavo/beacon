package io.beacon.userservice;

import com.github.javafaker.Faker;
import io.beacon.userservice.user.entity.User;

public class TestUserDataFactory {

  private static final Faker faker = new Faker();

  public static User createRandomUser() {
    return new User(
        faker.name().username(), faker.name().fullName(), faker.internet().password()
    );
  }

  public static User createUserWithUsername(String username) {
    return new User(
        username, faker.name().fullName(), faker.internet().password()
    );
  }
}
