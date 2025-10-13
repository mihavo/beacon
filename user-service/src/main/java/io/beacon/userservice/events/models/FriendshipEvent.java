package io.beacon.userservice.events.models;

import io.beacon.userservice.events.enums.FriendshipEventType;
import java.time.Instant;

public record FriendshipEvent(
    FriendshipEventType type,
    String userId,
    String friendId,
    Instant timestamp
) {

}
