package io.beacon.userservice.events;

import io.locationservice.authz.FriendshipPermissionService;
import io.locationservice.authz.events.models.FriendshipEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendshipEventsListener {

  private final FriendshipPermissionService permissionService;

  @KafkaListener(topics = "user-friendship-events")
  public void onEvent(FriendshipEvent event) {
    log.info("Received friendship event: {}", event);
    permissionService.handleFriendshipEvent(event);
  }


}
