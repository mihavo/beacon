package io.beacon.userservice.events;

import io.beacon.events.FriendshipEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendshipEventProducer {

  private final KafkaTemplate<String, FriendshipEvent> template;


  public Mono<Void> send(FriendshipEvent event) {
    return Mono.fromFuture(
        () -> template.send("user-friendship-events", event.userId(), event)).doOnSuccess(
        result -> log.info("Sent friendship event of type {} for user {} at: {}", event.type(),
            event.userId(), result.getRecordMetadata().timestamp())).then();
  }
}
