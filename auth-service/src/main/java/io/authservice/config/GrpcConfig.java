package io.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import userservice.UserServiceGrpc;

@Configuration
public class GrpcConfig {

  @Bean
  UserServiceGrpc.UserServiceBlockingStub stub(GrpcChannelFactory channels) {
    return UserServiceGrpc.newBlockingStub(channels.createChannel("local"));
  }
}
