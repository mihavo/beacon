package io.beacon.locationservice.web;

import io.beacon.config.NoAuthSecurityConfig;
import io.beacon.location.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestAuthUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class PublishControllerTest extends RedisTestBase {

  private WebTestClient webTestClient;

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

    @Autowired
    private ApplicationContext applicationContext;

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
        .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody().isEmpty();
  }

  @Test
  void testPublish_emptyLocationRecordsSet() {
    webTestClient.post()
        .uri("/")
        .bodyValue(Collections.emptySet())
        .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith(response -> {
          Assertions.assertNotNull(response.getResponseBody());
          String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
          assertThat(body).contains("must not be empty");
        });
  }

  @Test
  void testPublish_invalidLocationRecordsSet() {
    webTestClient.post()
        .uri("/")
        .bodyValue(Set.of(new PublishLocationRequest(new Coordinates(null, null), Instant.now())))
        .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith(response -> {
          Assertions.assertNotNull(response.getResponseBody());
          String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
          assertThat(body).contains("latitude: must not be null");
          assertThat(body).contains("longitude: must not be null");
        });
  }

  @Test
  void testPublish_invalidTimestamp() {
    webTestClient.post().uri("/")
        .bodyValue(Set.of(new PublishLocationRequest(new Coordinates(-37.8, -86.5214), Instant.now().plus(Duration.ofDays(1)))))
        .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .consumeWith(response -> {
          Assertions.assertNotNull(response.getResponseBody());
          String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);
          assertThat(body).contains("must be a date in the past or in the present");
        });
  }
}
