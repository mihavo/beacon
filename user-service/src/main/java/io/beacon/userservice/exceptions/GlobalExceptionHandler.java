package io.beacon.userservice.exceptions;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public Mono<ResponseEntity<APIErrorResponse>> handleUserNotFoundException(
      UserNotFoundException ex) {
    APIErrorResponse response = new APIErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()
        , OffsetDateTime.now());
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
  }

  @ExceptionHandler(AlreadyFriendsException.class)
  public Mono<ResponseEntity<APIErrorResponse>> handleAlreadyFriendsException(
      AlreadyFriendsException ex) {
    APIErrorResponse response = new APIErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()
        , OffsetDateTime.now());
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
  }


  @ExceptionHandler(ConnectionRequestExistsException.class)
  public Mono<ResponseEntity<APIErrorResponse>> handleConnectionRequestExistsException(
      ConnectionRequestExistsException ex) {
    APIErrorResponse response = new APIErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()
        , OffsetDateTime.now());
    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error", ex);
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
