package io.beacon.historyservice.locations.dto;

public record ClusteredLocation(
    int clusterId,
    Double longitude,
    Double latitude,
    Long visits
) {
}
