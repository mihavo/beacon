package io.beacon.locationservice.location;

import io.beacon.WithMockBeaconUser;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.location.service.LocationService;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestLocationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = "location-history-events")
public class UserLocationServiceTest extends RedisTestBase {

  @Autowired private PublishService publishService;
  @Autowired private LocationService locationService;

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

  @Autowired
  private ReactiveRedisConnectionFactory redisConnectionFactory;
  @Autowired private ReactiveRedisTemplate<String, Object> redisTemplate;

  @AfterEach
  void cleanup() {
    redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().block();
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void fetchRecent_shouldReturnMostRecentLocations() {
    Set<PublishLocationRequest> locations = TestLocationUtils.createSampleLocationCoordinates(null);
    TestLocationUtils.givenUserHasPublishedLocations(publishService, locations);
    StepVerifier.create(locationService.fetchRecent(UUID.fromString(TEST_USER_ID)))
        .recordWith(ArrayList::new)
        .expectNextCount(locations.size())
        .consumeRecordedWith(results -> {
          List<PublishLocationRequest> locationRequestResults =
              results.stream().map(result -> new PublishLocationRequest(result.getCoords(), result.getTimestamp())).toList();
          assertThat(locations).containsExactlyInAnyOrderElementsOf(locationRequestResults);
        })
        .verifyComplete();
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void fetchRecent_shouldReturnNoRecentLocations_whenNonePublished() {
    StepVerifier.create(locationService.fetchRecent(UUID.fromString(TEST_USER_ID))).expectNextCount(0).verifyComplete();
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void fetchRecent_shouldOnlyReturnLocationsForSpecificUser() {
    String otherUserId = UUID.randomUUID().toString();
    Set<PublishLocationRequest> locations = TestLocationUtils.createSampleLocationCoordinates(null);
    Set<PublishLocationRequest> otherLocations = TestLocationUtils.createSampleLocationCoordinates(null);
    TestLocationUtils.givenUserHasPublishedLocations(publishService, locations);
    TestLocationUtils.givenUserHasPublishedLocationsDirectly(redisTemplate, otherUserId, otherLocations);
    StepVerifier.create(locationService.fetchRecent(UUID.fromString(TEST_USER_ID)))
        .recordWith(ArrayList::new)
        .expectNextCount(locations.size())
        .consumeRecordedWith(results -> {
          List<PublishLocationRequest> locationRequestResults =
              results.stream().map(result -> new PublishLocationRequest(result.getCoords(), result.getTimestamp())).toList();
          assertThat(locations).containsExactlyInAnyOrderElementsOf(locationRequestResults);
        })
        .verifyComplete();
  }
}
