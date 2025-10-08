package io.beacon.userservice.connections.dto;

import java.time.Instant;

public record ConnectResponse(String message, Instant timestamp) {

}
