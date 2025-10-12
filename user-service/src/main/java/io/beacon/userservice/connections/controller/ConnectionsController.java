package io.beacon.userservice.connections.controller;

import io.beacon.userservice.connections.dto.AcceptRequest;
import io.beacon.userservice.connections.dto.AcceptResponse;
import io.beacon.userservice.connections.dto.ConnectResponse;
import io.beacon.userservice.connections.dto.ConnectionsInfo;
import io.beacon.userservice.connections.dto.DeclineRequest;
import io.beacon.userservice.connections.dto.DeclineResponse;
import io.beacon.userservice.connections.dto.RemoveConnectionResponse;
import io.beacon.userservice.connections.dto.UserStatusInfo;
import io.beacon.userservice.connections.service.ConnectionsService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

  @PostMapping("/{targetUserId}")
  public Mono<ResponseEntity<ConnectResponse>> connect(@PathVariable UUID targetUserId) {
    return connectionsService.connect(targetUserId)
        .map(response -> ResponseEntity.status(
        HttpStatus.CREATED).body(response));
  }

  @GetMapping("/{targetUserId}/status")
  public Mono<ResponseEntity<UserStatusInfo>> status(@PathVariable UUID targetUserId) {
    return connectionsService.getStatus(targetUserId)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @GetMapping
  public Mono<ResponseEntity<ConnectionsInfo>> connections() {
    return connectionsService.getConnections()
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }


  @GetMapping("/friends")
  public Mono<ResponseEntity<ConnectionsInfo>> friends() {
    return connectionsService.getFriends()
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }


  @PostMapping("/accept")
  public Mono<ResponseEntity<AcceptResponse>> accept(@RequestBody AcceptRequest request) {
    return connectionsService.accept(request.targetUserId())
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  @DeleteMapping("/decline")
  public Mono<ResponseEntity<DeclineResponse>> decline(@RequestBody DeclineRequest request) {
    return connectionsService.decline(request.targetUserId())
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @DeleteMapping("/{targetUserId}")
  public Mono<ResponseEntity<RemoveConnectionResponse>> remove(@PathVariable UUID targetUserId) {
    return connectionsService.removeFriend(targetUserId)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }
}
