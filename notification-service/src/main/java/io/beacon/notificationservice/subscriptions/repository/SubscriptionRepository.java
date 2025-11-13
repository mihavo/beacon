package io.beacon.notificationservice.subscriptions.repository;

import io.beacon.notificationservice.subscriptions.entity.Subscription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  List<Subscription> getSubscriptionsByUserId(UUID userId);

  int deleteSubscriptionsByUserId(UUID userId);

  @Query("SELECT fcmToken FROM Subscription WHERE userId = :userId")
  List<String> findUserRegistrationTokens(@Param("userId") UUID userId);
}
