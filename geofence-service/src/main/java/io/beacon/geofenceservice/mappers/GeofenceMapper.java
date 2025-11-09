package io.beacon.geofenceservice.mappers;

import io.beacon.geofenceservice.dto.CreateGeofenceResponse;
import io.beacon.geofenceservice.dto.GeofenceResponse;
import io.beacon.geofenceservice.entity.Geofence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GeofenceMapper {

  @Mapping(target = "geofence_id", source = "id")
  CreateGeofenceResponse toCreateResponse(Geofence geofence);

  @Mapping(target = "userId", source = "targetId")
  @Mapping(target = "centerLongitude", source = "center.x")
  @Mapping(target = "centerLatitude", source = "center.y")
  GeofenceResponse toResponse(Geofence geofence);
}