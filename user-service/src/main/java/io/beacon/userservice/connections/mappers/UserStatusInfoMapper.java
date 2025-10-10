package io.beacon.userservice.connections.mappers;

import io.beacon.userservice.connections.dto.UserStatusInfo;
import io.beacon.userservice.user.model.RelationshipTypes;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStatusInfoMapper {

  UserStatusInfo toUserStatusInfo(RelationshipTypes relationship);
}
