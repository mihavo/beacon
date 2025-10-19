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

  public static LocationEvent toLocationEvent(UUID userId, MapRecord<String, String, Object> record) {
    Coordinates coords = parseCoords(record);
    return new LocationEvent(userId.toString(), coords.latitude(), coords.longitude(),
        Instant.parse((String) record.getValue().get(
            "capturedAt")));
  }

  public static Coordinates parseCoords(MapRecord<String, String, Object> location) {
    double lat = Double.parseDouble((String) location.getValue().get("lat"));
    double lon = Double.parseDouble((String) location.getValue().get("lon"));
    return new Coordinates(lat, lon);
  }

  public static void setCoords(MapRecord<String, String, Object> record, Coordinates coords) {
    Map<String, Object> values = record.getValue();
    values.put("lon", coords.longitude());
    values.put("lat", coords.latitude());
  }

}
