package io.beacon.locationservice.entity;

import io.beacon.locationservice.models.Coordinates;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Location {
    UUID userId;
    Coordinates coords;
  Instant timestamp;
}
