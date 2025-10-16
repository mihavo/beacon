package io.beacon.historyservice.locations.mappers;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.util.UUID;

public final class LocationMapper {

  public static LocationHistory toLocationHistory(LocationEvent event) {
    LocationHistoryId id = LocationHistoryId.builder()
        .userId(UUID.fromString(event.userId()))
        .timestamp(event.timestamp()).build();
    return LocationHistory.builder()
        .id(id).latitude(event.latitude()).longitude(event.longitude()).build();
  }
}
