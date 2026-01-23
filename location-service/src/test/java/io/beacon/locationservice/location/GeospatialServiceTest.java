package io.beacon.locationservice.location;

import io.beacon.WithMockBeaconUser;
import io.beacon.location.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.location.geospatial.GeospatialService;
import io.beacon.locationservice.models.UserLocation;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestLocationUtils;
import locationservice.LocationServiceOuterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@EmbeddedKafka
@DirtiesContext
public class GeospatialServiceTest extends RedisTestBase {

  @Autowired
  private ReactiveRedisConnectionFactory redisConnectionFactory;

  @MockitoBean AuthGrpcClient grpcClient;

  @Autowired
  private PublishService publishService;
  @Autowired private GeospatialService geospatialService;

  @AfterEach
  public void cleanup() {
    redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().block();
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void searchInBoundingBox() {
    LocationServiceOuterClass.BoundingBox bbox = TestLocationUtils.generateRandomBoundingBox(0.5);
    Set<PublishLocationRequest> locations =
        TestLocationUtils.createSampleLocationCoordinatesInBbox(bbox, 5);
    TestLocationUtils.givenUserHasPublishedLocations(publishService, locations);
    StepVerifier.create(geospatialService.searchInBoundingBox(bbox))
        .recordWith(ArrayList::new)
        .expectNextCount(1)
        .consumeRecordedWith(results -> {
          UserLocation last_seen = results.iterator().next();
          PublishLocationRequest expected =
              locations.stream().max(Comparator.comparing(PublishLocationRequest::capturedAt)).orElseThrow();
          assertEquals(expected.capturedAt(), last_seen.location().getTimestamp());
          assertThat(expected.coords().longitude()).isCloseTo(last_seen.location().getCoords().longitude(), within(0.001));
          assertThat(expected.coords().latitude()).isCloseTo(last_seen.location().getCoords().latitude(), within(0.001));
          assertEquals(TEST_USER_ID, last_seen.userId());
        })
        .verifyComplete();
  }
}
