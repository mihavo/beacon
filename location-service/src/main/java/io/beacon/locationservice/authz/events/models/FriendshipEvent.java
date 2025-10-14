package io.beacon.locationservice.authz.events.models;

import io.beacon.locationservice.authz.enums.FriendshipEventType;
import java.time.Instant;

public record FriendshipEvent(
    FriendshipEventType type,
    String userId,
    String friendId,
    Instant timestamp
) {

}
