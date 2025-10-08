package io.beacon.userservice.exceptions;

public class AlreadyFriendsException extends RuntimeException {

  public AlreadyFriendsException() {
  }

  public AlreadyFriendsException(String message) {
    super(message);
  }
}
