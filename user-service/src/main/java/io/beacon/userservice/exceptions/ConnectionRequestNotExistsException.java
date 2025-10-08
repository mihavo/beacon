package io.beacon.userservice.exceptions;

public class ConnectionRequestNotExistsException extends RuntimeException {

  public ConnectionRequestNotExistsException() {
  }

  public ConnectionRequestNotExistsException(String message) {
    super(message);
  }
}
