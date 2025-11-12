package io.beacon.notificationservice.subscriptions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;

@Entity
@Builder
@Table(name = "subscriptions")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "token", nullable = false, unique = true)
  String fcmToken;

  @Column(name = "user_id", nullable = false, unique = true)
  UUID user_id;

  @Column(name = "device_id", nullable = false, unique = true)
  UUID device_id;
}
