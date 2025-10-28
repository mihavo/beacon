package io.beacon.locationservice.grpc.clients;

import io.beacon.locationservice.models.UserInfo;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;

@Component
@RequiredArgsConstructor
public class UserGrpcClient {

  private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  public List<UserInfo> getUserFriends(String userId) {
    UserServiceOuterClass.GetUserFriendsResponse response =
        userServiceStub.getUserFriends(UserServiceOuterClass.GetUserFriendsRequest.newBuilder().setUserId(userId).build());
    return response.getFriendsList()
        .stream()
        .map(friend -> new UserInfo(friend.getUserId(), friend.getFullName(), friend.getUsername(),
            Instant.ofEpochMilli(friend.getFriendsSince())))
        .toList();
  }
}
