package io.beacon.userservice.user.grpc;

import io.beacon.userservice.user.entity.User;
import io.beacon.userservice.user.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import userservice.UserServiceGrpc;
import userservice.UserServiceOuterClass.CreateUserRequest;
import userservice.UserServiceOuterClass.CreateUserResponse;
import userservice.UserServiceOuterClass.GetUserByUsernameRequest;
import userservice.UserServiceOuterClass.GetUserByUsernameResponse;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {

  private final UserRepository userRepository;

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
    userRepository.findByUsername(request.getUsername()).map(
        user -> GetUserByUsernameResponse.newBuilder().setUsername(user.getUsername())
            .setFullName(user.getFullName()).setId(user.getId().toString())
            .setPasswordHash(user.getPassword())
            .build()).subscribe(
        responseObserver::onNext,
        responseObserver::onError,
        responseObserver::onCompleted
    );
  }
}

