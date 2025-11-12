package io.beacon.notificationservice.subscriptions.service;

import io.beacon.notificationservice.subscriptions.dto.SubscriptionRequest;
import io.beacon.notificationservice.subscriptions.dto.SubscriptionResponse;
import io.beacon.notificationservice.subscriptions.entity.Subscription;
import io.beacon.notificationservice.subscriptions.repository.SubscriptionRepository;
import io.beacon.security.utils.AuthUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;

  public Mono<SubscriptionResponse> subscribe(SubscriptionRequest request) {
    //TODO: get real device id
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          Subscription subscription = Subscription.builder()
              .user_id(userId)
              .fcmToken(request.registration_token())
              .device_id(UUID.randomUUID())
              .build();
          subscriptionRepository.save(subscription);
          return new SubscriptionResponse("Subscribed.");
        }).subscribeOn(Schedulers.boundedElastic()));
  }
}
