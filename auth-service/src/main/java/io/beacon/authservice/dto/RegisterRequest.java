package io.beacon.authservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotNull
    @Size(min = 4)
    String username,
    @NotNull
    @Size(min = 6) String fullName,
    @NotNull
    @Size(min = 8)
    String password) {

}
