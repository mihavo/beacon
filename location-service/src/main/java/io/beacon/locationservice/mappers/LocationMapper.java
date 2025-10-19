package io.beacon.locationservice.mappers;

import io.beacon.events.LocationEvent;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.models.Coordinates;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.connection.stream.MapRecord;

public final class LocationMapper {

  public static Location toLocation(MapRecord<String, String, Object> locationRecord) {
    Map<String, Object> values = locationRecord.getValue();
    return Location.builder()
        .timestamp(Instant.parse((String) values.get("capturedAt")))
        .coords(new Coordinates(Double.valueOf(values.get("lat").toString()),
            Double.valueOf(values.get("lon").toString()))).build();
  }

  public static LocationEvent toLocationEvent(UUID userId, Location location) {
    return new LocationEvent(userId.toString(), location.getCoords().latitude(),
        location.getCoords()
            .longitude(), location.getTimestamp());
  }

}
