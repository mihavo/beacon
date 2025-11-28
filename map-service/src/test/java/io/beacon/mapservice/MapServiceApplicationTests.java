package io.beacon.mapservice;

import io.beacon.mapservice.clients.AuthGrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"geofence-alerts", "location-stream-events"})
class MapServiceApplicationTests {

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

  @Test
    void contextLoads() {
    }

}
