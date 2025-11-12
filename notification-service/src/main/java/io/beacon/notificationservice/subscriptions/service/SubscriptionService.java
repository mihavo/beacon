package io.beacon.notificationservice.subscriptions.service;

import io.beacon.notificationservice.subscriptions.dto.GetSubscriptionsResponse;
import io.beacon.notificationservice.subscriptions.dto.SubscribeResponse;
import io.beacon.notificationservice.subscriptions.dto.SubscriptionRequest;
import io.beacon.notificationservice.subscriptions.dto.SubscriptionResponse;
import io.beacon.notificationservice.subscriptions.dto.UnsubscribeResponse;
import io.beacon.notificationservice.subscriptions.entity.Subscription;
import io.beacon.notificationservice.subscriptions.mappers.SubscriptionMapper;
import io.beacon.notificationservice.subscriptions.repository.SubscriptionRepository;
import io.beacon.security.utils.AuthUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionMapper subscriptionMapper;

  public Mono<SubscribeResponse> subscribe(SubscriptionRequest request) {
    //TODO: get real device id
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          Subscription subscription = Subscription.builder()
              .user_id(userId)
              .fcmToken(request.registration_token())
              .device_id(UUID.randomUUID())
              .build();
          subscriptionRepository.save(subscription);
          return new SubscribeResponse("Subscribed.");
        }).subscribeOn(Schedulers.boundedElastic()));
  }

  public Mono<GetSubscriptionsResponse> getSubscriptions() {
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          List<Subscription> subscriptions = subscriptionRepository.getSubscriptionsByUser_id(userId);
          List<SubscriptionResponse> responses = subscriptions.stream().map(subscriptionMapper::toSubscriptionResponse).toList();
          return new GetSubscriptionsResponse(responses, responses.size());
        }).subscribeOn(Schedulers.boundedElastic())
    );
  }

  public Mono<UnsubscribeResponse> unsubscribe() {
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          int unsubscribeCount = subscriptionRepository.deleteSubscriptionsByUser_id(userId);
          return new UnsubscribeResponse("Unsubscribed " + unsubscribeCount + " devices");
        }).subscribeOn(Schedulers.boundedElastic())
    );
  }
}
