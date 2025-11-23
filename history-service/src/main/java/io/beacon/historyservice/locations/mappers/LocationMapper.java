package io.beacon.historyservice.locations.mappers;

import io.beacon.events.LocationEvent;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public final class LocationMapper {

  static GeometryFactory geometry = new GeometryFactory(new PrecisionModel(), 4326);

  public static LocationHistory toLocationHistory(LocationEvent event) {
    LocationHistoryId id =
        LocationHistoryId.builder().userId(UUID.fromString(event.userId())).timestamp(event.timestamp()).build();
    return LocationHistory.builder()
        .id(id)
        .latitude(event.latitude())
        .longitude(event.longitude())
        .location(geometry.createPoint(new Coordinate(event.longitude(), event.latitude())))
        .build();
  }
}
