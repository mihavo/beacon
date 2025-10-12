package io.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@EnableDiscoveryClient
@ImportGrpcClients(basePackages = "io.beacon")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
