package io.locationservice.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class APIErrorResponse {
    private HttpStatus status;

    private String message;

    private ZonedDateTime timestamp;
}
