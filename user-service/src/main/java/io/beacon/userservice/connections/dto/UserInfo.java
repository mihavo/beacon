package io.beacon.userservice.connections.dto;

import io.beacon.userservice.user.model.ConnectionType;
import java.time.Instant;

public record UserInfo(String fullName, String username, ConnectionType status,
                       Instant lastConnectionTimestamp) {

}
