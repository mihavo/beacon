package io.beacon.locationservice.authz.events;

import io.beacon.events.FriendshipEvent;
import io.beacon.locationservice.authz.FriendshipPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendshipEventsListener {

  private final FriendshipPermissionService permissionService;

  @KafkaListener(topics = "user-friendship-events")
  public Mono<Void> listen(FriendshipEvent event) {
    log.info("Received friendship event: {}", event);
    return permissionService.handleFriendshipEvent(event);
  }


}
