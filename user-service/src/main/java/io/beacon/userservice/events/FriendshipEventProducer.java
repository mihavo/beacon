package io.beacon.userservice.events;

import io.beacon.userservice.events.models.FriendshipEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FriendshipEventProducer {

  private final KafkaTemplate<String, FriendshipEvent> template;


  public Mono<Void> send(FriendshipEvent event) {
    return Mono.fromFuture(
        () -> template.send("user-friendship-events", event.userId(), event)).then();
  }
}
