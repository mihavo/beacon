package io.beacon.userservice;

import io.beacon.userservice.config.UserTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;

@SpringBootTest
@AutoConfigureInProcessTransport
class UserServiceApplicationTests extends UserTestsBase {

  @Test
    void contextLoads() {
    }

}
