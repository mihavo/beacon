package io.locationservice.request;

import io.locationservice.model.Coordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;
import java.util.UUID;

public record PublishLocationRequest(
        UUID userId,
        @Valid
        Coordinates coords,
        @PastOrPresent
        Instant capturedAt
) {

}
