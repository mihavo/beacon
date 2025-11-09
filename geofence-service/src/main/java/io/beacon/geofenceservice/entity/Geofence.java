package io.beacon.geofenceservice.entity;

import io.beacon.geofenceservice.enums.TriggerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "geofences")
public class Geofence {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "userId", updatable = false)
  private UUID userId;

  @Column(name = "target_id", updatable = false)
  private UUID targetId;

  @Column(name = "center", columnDefinition = "geometry(Point,4326)", updatable = false)
  private Point center;

  @Column(name = "radius_meters", updatable = false)
  private Double radius_meters;

  @Column(name = "trigger_type")
  private TriggerType triggerType;

  @Column(name = "isActive")
  @Builder.Default
  private Boolean isActive = Boolean.TRUE;
}
