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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionMapper subscriptionMapper;

  @Transactional
  public Mono<SubscribeResponse> subscribe(SubscriptionRequest request) {
    //TODO: get real device id
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        ensureSubscriptionExistence(userId).then(Mono.fromCallable(() -> {
          Subscription subscription = Subscription.builder()
              .userId(userId)
              .fcmToken(request.registration_token())
              .deviceId(UUID.randomUUID())
              .build();
          subscriptionRepository.save(subscription);
          return new SubscribeResponse("Subscribed.");
        }).subscribeOn(Schedulers.boundedElastic())));
  }

  public Mono<GetSubscriptionsResponse> getSubscriptions() {
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          List<Subscription> subscriptions = subscriptionRepository.getSubscriptionsByUserId(userId);
          List<SubscriptionResponse> responses = subscriptions.stream().map(subscriptionMapper::toSubscriptionResponse).toList();
          return new GetSubscriptionsResponse(responses, responses.size());
        }).subscribeOn(Schedulers.boundedElastic())
    );
  }

  public Mono<UnsubscribeResponse> unsubscribe() {
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> {
          long unsubscribeCount = subscriptionRepository.deleteSubscriptionsByUserId(userId);
          if (unsubscribeCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No subscriptions active to unsubscribe from.");
          }
          return new UnsubscribeResponse("Unsubscribed " + unsubscribeCount + " devices");
        }).subscribeOn(Schedulers.boundedElastic())
    );
  }

  public Mono<String> findUserRegistrationToken(UUID userId) {
    return Mono.fromCallable(() -> subscriptionRepository.findUserRegistrationTokens(userId))
        .map(List::getFirst)
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Mono<Void> ensureSubscriptionExistence(UUID userId) {
    return Mono.fromCallable(() -> subscriptionRepository.existsSubscriptionsByUserId(userId))
        .subscribeOn(Schedulers.boundedElastic())
        .filter(exists -> !exists)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Subscriptions already exist for the current user."))).then();
  }
}
