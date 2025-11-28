package io.beacon.locationservice;

import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"location-history-events"})
class LocationServiceApplicationTests {

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

    @Test
    void contextLoads() {
    }

}
