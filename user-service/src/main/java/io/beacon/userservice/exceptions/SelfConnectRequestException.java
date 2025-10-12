package io.beacon.userservice.exceptions;

public class SelfConnectRequestException extends RuntimeException {

  public SelfConnectRequestException(String message) {
    super(message);
  }

  public SelfConnectRequestException() {
    super();
  }
}
