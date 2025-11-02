package io.beacon.mapservice.stream;

import io.beacon.events.FriendshipEvent;
import io.beacon.mapservice.service.FriendsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendshipEventsListener {

  private final FriendsService friendsService;

  @KafkaListener(topics = "user-friendship-events", containerFactory = "friendshipKafkaListenerContainerFactory")
  public Mono<Void> listen(FriendshipEvent event) {
    log.info("Received friendship event: {}", event);
    return friendsService.handleFriendshipEvent(event);
  }
}
