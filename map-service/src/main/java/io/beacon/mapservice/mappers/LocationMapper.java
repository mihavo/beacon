package io.beacon.mapservice.mappers;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.models.UserLocation;
import java.time.Instant;
import locationservice.LocationServiceOuterClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

  @Mapping(source = "latitude", target = "coords.latitude")
  @Mapping(source = "longitude", target = "coords.longitude")
  UserLocation toUserLocation(LocationEvent event);

  @Mapping(source = "latitude", target = "coords.latitude")
  @Mapping(source = "longitude", target = "coords.longitude")
  UserLocation toUserLocation(LocationServiceOuterClass.UserLocation grpcLocation);

  default Instant map(long value) {
    return Instant.ofEpochMilli(value);
  }
}
