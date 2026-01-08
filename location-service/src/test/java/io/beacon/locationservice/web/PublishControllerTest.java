package io.beacon.locationservice.web;

import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestLocationUtils;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@EmbeddedKafka(partitions = 1, topics = {"location-history-events"})
public class PublishControllerTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void testPublish_locationsPublished() {
    Set<PublishLocationRequest> locations = TestLocationUtils.createSampleLocationCoordinates(5);
    webTestClient.post().uri("/locations/").bodyValue(locations).exchange().expectStatus().isOk();
  }
}
