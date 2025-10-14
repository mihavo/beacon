package io.beacon.locationservice.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public Mono<ResponseEntity<APIErrorResponse>> handleConstraintViolation(
      ConstraintViolationException ex) {
    String messages = ex.getConstraintViolations().stream().map(v ->
            StreamSupport.stream(v.getPropertyPath().spliterator(), false).filter(Objects::nonNull)
                .map(Path.Node::getName).reduce((_, second) -> second).get() + ": " + v.getMessage())
        .collect(Collectors.joining(", "));
    return Mono.just(ResponseEntity.badRequest().body(
        APIErrorResponse.builder().message(messages).status(HttpStatus.BAD_REQUEST)
            .timestamp(ZonedDateTime.now()).build()));
  }
}
