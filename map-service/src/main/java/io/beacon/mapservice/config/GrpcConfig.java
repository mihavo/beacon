package io.beacon.mapservice.config;

import authservice.AuthServiceGrpc;
import authservice.AuthServiceGrpc.AuthServiceBlockingStub;
import locationservice.LocationServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

  @Bean
  AuthServiceBlockingStub userStub(GrpcChannelFactory channels) {
    return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth"));
  }

  @Bean
  LocationServiceGrpc.LocationServiceBlockingStub locationStub(GrpcChannelFactory channels) {
    return LocationServiceGrpc.newBlockingStub(channels.createChannel("location"));
  }
}
