package io.beacon.userservice.grpc.service;

import io.beacon.userservice.connections.mappers.UserInfoMapper;
import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass;
import userservice.UserServiceOuterClass.CreateUserRequest;
import userservice.UserServiceOuterClass.CreateUserResponse;
import userservice.UserServiceOuterClass.GetUserByUsernameRequest;
import userservice.UserServiceOuterClass.GetUserByUsernameResponse;
import userservice.UserServiceOuterClass.GetUserFriendsRequest;
import userservice.UserServiceOuterClass.GetUserFriendsResponse;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {

  private final UserRepository userRepository;
  private final UserInfoMapper userInfoMapper;

  @Override
  public void createUser(CreateUserRequest request,
      StreamObserver<CreateUserResponse> responseObserver) {
    User user = new User(request.getUsername(), request.getFullName(), request.getPasswordHash());
    userRepository.save(user).map(
            savedUser -> CreateUserResponse.newBuilder().setId(savedUser.getId().toString()).build())
        .doOnNext(responseObserver::onNext).doOnTerminate(responseObserver::onCompleted)
        .subscribe();
  }

  @Override
  public void getUserByUsername(GetUserByUsernameRequest request,
      StreamObserver<GetUserByUsernameResponse> responseObserver) {
    userRepository.findUserByUsername(request.getUsername()).switchIfEmpty(
            Mono.error(Status.NOT_FOUND.withDescription("User not found").asRuntimeException())).map(
            user -> GetUserByUsernameResponse.newBuilder().setUsername(user.getUsername())
                .setFullName(user.getFullName()).setId(user.getId().toString())
                .setPasswordHash(user.getPassword()).build())
        .subscribe(responseObserver::onNext, error -> {
          if (error instanceof StatusRuntimeException) {
            responseObserver.onError(error);
          } else {
            responseObserver.onError(
                Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
          }
        }, responseObserver::onCompleted);
  }

  @Override public void getUserFriends(GetUserFriendsRequest request,
      StreamObserver<GetUserFriendsResponse> responseObserver) {
    userRepository.getFriends(UUID.fromString(request.getUserId())).switchIfEmpty(
            Mono.error(Status.NOT_FOUND.withDescription("User not found").asRuntimeException()))
        .collectList().map(users -> {
          List<UserServiceOuterClass.User> friends = users.stream().map(userInfoMapper::toGrpcUser).toList();
          return GetUserFriendsResponse.newBuilder().addAllFriends(friends).build();
        })
        .subscribe(responseObserver::onNext, error -> {
          if (error instanceof StatusRuntimeException) {
            responseObserver.onError(error);
          } else {
            responseObserver.onError(
                Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
          }
        }, responseObserver::onCompleted);
  }
}

