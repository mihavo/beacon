package io.beacon.userservice.config;

import io.beacon.userservice.user.websocket.UserSearchWebSocketHandler;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Configuration
public class WebSocketConfig {
  @Bean
  public HandlerMapping webSocketMapping(UserSearchWebSocketHandler handler) {
    Map<String, WebSocketHandler> map = Map.of("/ws/search", handler);
    return new SimpleUrlHandlerMapping(map, 10);
  }

  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }
}
