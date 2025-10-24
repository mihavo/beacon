package io.beacon.historyservice.exceptions;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class APIErrorResponse {
  private HttpStatus status;

  private String message;

  private ZonedDateTime timestamp;
}
