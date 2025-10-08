package io.beacon.userservice.user.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserRequest(
    @NotNull
    @Length(min = 6, message = "Username must have at least 6 characters")
    String username,
    @NotNull
    @Length(min = 4, message = "Full Name must have at least 4 characters")
    String fullName
) {

}
