package io.beacon.userservice.connections.controller;

import io.beacon.userservice.connections.dto.ConnectRequest;
import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.connections.service.ConnectionsService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/connections")
public class ConnectionsController {

  private final ConnectionsService connectionsService;

  @PostMapping("/{targetUserId}/connect")
  public Mono<ResponseEntity<ConnectResponse>> connect(@PathVariable UUID targetUserId,
      @RequestBody ConnectRequest request) {
    //TODO: remove user id from request body and get it from security contextHolder
    return connectionsService.connect(targetUserId, request.userId())
        .map(response -> ResponseEntity.status(
        HttpStatus.CREATED).body(response));
  }
}
