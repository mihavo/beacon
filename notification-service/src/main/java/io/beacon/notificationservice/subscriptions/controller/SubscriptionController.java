package io.beacon.notificationservice.subscriptions.controller;

import io.beacon.notificationservice.subscriptions.dto.SubscriptionRequest;
import io.beacon.notificationservice.subscriptions.dto.SubscriptionResponse;
import io.beacon.notificationservice.subscriptions.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping("/subscribe")
  public Mono<ResponseEntity<SubscriptionResponse>> subscribe(@Valid @RequestBody SubscriptionRequest request) {
    return subscriptionService.subscribe(request).map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }
}
