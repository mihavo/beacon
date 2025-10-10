package io.beacon.userservice.connections.dto;

import java.time.Instant;

public record UserInfo(String fullName, String username, String status,
                       Instant lastConnectionTimestamp) {

}
