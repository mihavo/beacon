package io.locationservice.authz.events;

import io.locationservice.authz.events.models.FriendshipEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FriendshipEventsListener {

  @KafkaListener(topics = "user-friendship-events")
  public void onEvent(FriendshipEvent event) {
    log.info("Received friendship event: {}", event);

  }


}
