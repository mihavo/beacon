package io.beacon.userservice.user.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.beacon.userservice.user.mappers.UserMapper;
import io.beacon.userservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Slf4j @Component
@RequiredArgsConstructor
public class UserSearchWebSocketHandler implements WebSocketHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UserMapper mapper;
  private final UserService userService;

  @Override
  @NonNull
  public Mono<Void> handle(@NonNull WebSocketSession session) {
    Flux<String> queries = session.receive()
        .map(WebSocketMessage::getPayloadAsText)
        .filter(q -> !q.isBlank());

    Flux<WebSocketMessage> results = queries
        .switchMap(query ->
            userService.search(query)
                .map(result -> {
                  try {
                    return session.textMessage(objectMapper.writeValueAsString(result));
                  } catch (Exception e) {
                    log.debug(e.getMessage());
                    return session.textMessage("{}");
                  }
                })
        );

    return session.send(results);
  }
}
