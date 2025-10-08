package io.beacon.userservice.exceptions;

import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;

public record APIErrorResponse(
    HttpStatus status,
    String message,
    OffsetDateTime timestamp
) {

}
