package io.beacon.events;

import io.beacon.events.enums.FriendshipEventType;
import java.time.Instant;

public record FriendshipEvent(
    FriendshipEventType type,
    String userId,
    String friendId,
    Instant timestamp
) {

}
