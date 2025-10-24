package io.beacon.historyservice.exceptions;

public class InvalidTimeRangeException extends RuntimeException {
  public InvalidTimeRangeException(String message) {
    super(message);
  }
}
