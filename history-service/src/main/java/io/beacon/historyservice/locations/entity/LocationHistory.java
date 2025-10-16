package io.beacon.historyservice.locations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "location_history")
public class LocationHistory {

  @EmbeddedId
  private LocationHistoryId id;

  @Column(name = "latitude")
  @DecimalMin(value = "-90.0")
  @DecimalMax(value = "90.0")
  @NotNull
  private Double latitude;

  @Column(name = "longitude")
  @DecimalMin(value = "-180.0")
  @DecimalMax(value = "180.0")
  @NotNull
  private Double longitude;
}
