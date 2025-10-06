package io.locationservice.entity;

import io.locationservice.model.Coordinates;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@AllArgsConstructor
@Builder
@Value
public class Location {
    UUID userId;
    Coordinates coords;
    Instant instant;
}
