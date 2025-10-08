package io.beacon.userservice.exceptions;

public class ConnectionRequestExistsException extends RuntimeException {

  public ConnectionRequestExistsException() {
  }

  public ConnectionRequestExistsException(String message) {
    super(message);
  }
}
