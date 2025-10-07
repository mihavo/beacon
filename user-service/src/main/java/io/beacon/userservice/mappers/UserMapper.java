package io.beacon.userservice.mappers;

import io.beacon.userservice.dto.UserResponse;
import io.beacon.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  

  UserResponse toUserResponse(User user);

}
