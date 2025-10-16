package io.beacon.historyservice.locations.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LocationHistoryId implements Serializable {

  private UUID userId;

  private Instant timestamp;
}
