package io.beacon.locationservice.mappers;

import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.utils.CacheUtils;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.redis.connection.stream.MapRecord;

public final class LocationMapper {

  public static Location toLocation(MapRecord<String, String, Object> locationRecord) {
    Map<String, Object> values = locationRecord.getValue();
    String streamId = Objects.requireNonNull(locationRecord.getStream());
    return Location.builder().userId(CacheUtils.extractUserId(streamId))
        .instant(CacheUtils.extractCapturedAtTimestamp(locationRecord.getId().getValue()))
        .coords(new Coordinates(Double.valueOf(values.get("lat").toString()),
            Double.valueOf(values.get("lon").toString()))).build();
  }

}
