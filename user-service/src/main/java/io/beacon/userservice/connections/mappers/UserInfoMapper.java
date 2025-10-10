package io.beacon.userservice.connections.mappers;

import io.beacon.userservice.connections.dto.UserStatusInfo;
import io.beacon.userservice.user.model.ConnectionType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

  UserStatusInfo toUserStatusInfo(ConnectionType relationship);
}
