package io.beacon.geofenceservice;

import io.beacon.geofenceservice.clients.AuthGrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureTestDatabase()
@EmbeddedKafka
@DirtiesContext
class GeofenceServiceApplicationTests {

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

    @Test
    void contextLoads() {
    }

}
