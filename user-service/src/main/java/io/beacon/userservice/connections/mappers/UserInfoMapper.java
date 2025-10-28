package io.beacon.userservice.connections.mappers;

import io.beacon.userservice.connections.dto.UserInfo;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import userservice.UserServiceOuterClass;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

  @Mapping(source = "lastConnectionTimestamp", target = "friendsSince")
  UserServiceOuterClass.User toGrpcUser(UserInfo userInfo);

  default Instant map(long value) {
    return Instant.ofEpochMilli(value);
  }
}
