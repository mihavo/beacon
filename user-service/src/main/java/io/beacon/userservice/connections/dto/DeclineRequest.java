package io.beacon.userservice.connections.dto;

import java.util.UUID;

public record DeclineRequest(UUID userId, UUID targetUserId) {

}
