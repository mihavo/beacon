package io.beacon.locationservice.location;

import io.beacon.WithMockBeaconUser;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.location.geospatial.GeospatialService;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestLocationUtils;
import java.util.List;
import java.util.Set;
import locationservice.LocationServiceOuterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = "location-history-events")
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
        .expectNextCount(5)
        .consumeRecordedWith(results -> {
          List<PublishLocationRequest> locationRequestResults =
              results.stream().map(result -> new PublishLocationRequest(result.location().getCoords(), result.location()
                  .getTimestamp())).toList();
          assertThat(locations).containsExactlyInAnyOrderElementsOf(locationRequestResults);
        })
        .verifyComplete();
  }
}
