package io.beacon.historyservice.locations.dto;

import java.time.Instant;

public record LocationHistoryResponse(
    Instant timestamp,
    Double longitude,
    Double latitude) {
}
