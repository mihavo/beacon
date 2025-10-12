package io.beacon.userservice.config;

import authservice.AuthServiceGrpc;
import authservice.AuthServiceGrpc.AuthServiceBlockingStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

  @Bean
  AuthServiceBlockingStub stub(GrpcChannelFactory channels) {
    return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth"));
  }

}
