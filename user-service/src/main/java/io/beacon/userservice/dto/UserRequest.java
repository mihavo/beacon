package io.beacon.userservice.dto;

public record UserRequest(
    String username,
    String fullName
) {

}
