package io.beacon.locationservice.request;

import io.beacon.locationservice.models.Coordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record PublishLocationRequest(
        @Valid
        Coordinates coords,
        @PastOrPresent
        Instant capturedAt
) {

}
