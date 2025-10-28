package io.beacon.locationservice.models;

import java.time.Instant;

public record UserInfo(String userId, String fullName, String username,
                       Instant friendsSince) {

}
