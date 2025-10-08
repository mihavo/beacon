package io.beacon.userservice.user.mappers;

import io.beacon.userservice.user.dto.UserResponse;
import io.beacon.userservice.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  

  UserResponse toUserResponse(User user);

}
