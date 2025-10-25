package io.beacon.historyservice.locations.dto;

public record ClusteredLocation(
    int clusterId,
    double longitude,
    double latitude,
    Long visits
) {
}
