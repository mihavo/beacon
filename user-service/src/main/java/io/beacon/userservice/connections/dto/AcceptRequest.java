package io.beacon.userservice.connections.dto;

import java.util.UUID;

public record AcceptRequest(UUID userId,// TODO: delete after  auth implementation
                            UUID targetUserId) {

}
