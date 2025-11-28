package io.beacon.notificationservice;

import io.beacon.notificationservice.clients.AuthGrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"geofence-alerts"})
class NotificationServiceApplicationTests {

  @MockitoBean AuthGrpcClient authGrpcClient;

  @Test
    void contextLoads() {
    }

}
