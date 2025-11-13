package io.beacon.notificationservice.subscriptions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscriptions")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "token", nullable = false, unique = true)
  String fcmToken;

  @Column(name = "user_id", nullable = false, unique = true)
  UUID userId;

  @Column(name = "device_id", nullable = false, unique = true)
  UUID deviceId;
}
