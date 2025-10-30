package io.beacon.locationservice.config;

import authservice.AuthServiceGrpc;
import authservice.AuthServiceGrpc.AuthServiceBlockingStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import userservice.UserServiceGrpc;

@Configuration
public class GrpcConfig {

  @Bean
  AuthServiceBlockingStub authStub(GrpcChannelFactory channels) {
    return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth"));
  }

  @Bean
  UserServiceGrpc.UserServiceBlockingStub userStub(GrpcChannelFactory channels) {
    return UserServiceGrpc.newBlockingStub(channels.createChannel("user"));
  }
}
