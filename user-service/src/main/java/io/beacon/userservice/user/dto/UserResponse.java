package io.beacon.userservice.user.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String fullName
) {

}
