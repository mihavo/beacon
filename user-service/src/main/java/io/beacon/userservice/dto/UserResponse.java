package io.beacon.userservice.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String fullName
) {

}
