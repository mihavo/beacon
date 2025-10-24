package io.beacon.historyservice.locations.mappers;

import io.beacon.historyservice.locations.dto.LocationHistoryResponse;
import io.beacon.historyservice.locations.entity.LocationHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationHistoryMapper {

  @Mapping(source = "id.timestamp", target = "timestamp")
  LocationHistoryResponse toLocationHistoryResponse(LocationHistory history);
}
