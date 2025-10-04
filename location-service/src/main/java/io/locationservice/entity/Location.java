package io.locationservice.entity;

import io.locationservice.model.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Value
public class Location {
    UUID userId;
    Coordinates coords;
    Instant instant;
}
