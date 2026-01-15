package io.beacon.locationservice.web;

import io.beacon.locationservice.config.NoAuthSecurityConfig;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.utils.TestAuthUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"location-history-events"})
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class PublishControllerTest extends RedisTestBase {

  private WebTestClient webTestClient;

  @MockitoBean
  private AuthGrpcClient authGrpcClient;
  @Autowired private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient
        .bindToApplicationContext(applicationContext)
        .configureClient()
        .baseUrl("http://localhost:8080")
        .build();
  }

  @Test
  void testPublish_locationsPublished() {
    var locations = TestLocationUtils.createSampleLocationCoordinates(5);
    webTestClient.post()
        .uri("/")
        .bodyValue(locations)
        .header("Authorization", TestAuthUtils.createMockAuthHeader())
        .exchange()
        .expectStatus()
        .isOk();
  }
}
