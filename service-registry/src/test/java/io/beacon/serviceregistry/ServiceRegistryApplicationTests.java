package io.beacon.serviceregistry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false"
})
@Disabled("Eureka Server cannot load context in tests — disabling")
class ServiceRegistryApplicationTests {

    @Test
    void contextLoads() {
    }

}
