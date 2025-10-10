package io.beacon.userservice.connections.dto;

import io.beacon.userservice.user.model.ConnectionType;

public record UserStatusInfo(ConnectionType status) {

}
