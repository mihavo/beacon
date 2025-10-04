package io.locationservice.request;

import io.locationservice.model.Coordinates;

import java.time.Instant;
import java.util.UUID;

public record PublishLocationRequest(
        UUID userId,
        Coordinates coords,
        Instant capturedAt
) {

}
