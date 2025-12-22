package io.beacon.locationservice.publish;

import io.beacon.WithMockBeaconUser;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.CacheUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"location-history-events"})
public class PublishLocationServiceTest extends RedisTestBase {

  @Autowired private PublishService publishService;
  @Autowired private ReactiveRedisTemplate<String, Object> redisTemplate;

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void publish_shouldPublishLocationRecords() {
    Set<PublishLocationRequest> request = TestLocationUtils.createSampleLocationCoordinates(null);
    StepVerifier.create(publishService.publish(request)).expectNextCount(1).verifyComplete();

    UUID userId = UUID.fromString(TEST_USER_ID);
    String cacheLocationKey = CacheUtils.buildLocationStreamKey(userId);
    List<MapRecord<String, Object, Object>> streamRecords =
        redisTemplate.opsForStream()
            .range(cacheLocationKey, Range.unbounded())
            .collectList()
            .block();

    assertNotNull(streamRecords);
    assertEquals(streamRecords.size(), request.size());

    streamRecords.forEach(record -> {
      assertThat(record.getValue())
          .containsKeys("lat", "lon", "capturedAt")
          .extracting("lat", "lon")
          .allMatch(coord -> coord instanceof Double);
    });

    PublishLocationRequest latestLocationRecord =
        request.stream().max(Comparator.comparing(PublishLocationRequest::capturedAt)).orElseThrow();
    Point savedPoint = redisTemplate.opsForGeo()
        .position(CacheUtils.getLocationGeospatialKey(),
            CacheUtils.buildGeospatialMember(userId))
        .block();

    assertThat(savedPoint).isNotNull();
    assertThat(savedPoint.getX()).isEqualTo(latestLocationRecord.coords().longitude());
    assertThat(savedPoint.getY()).isEqualTo(latestLocationRecord.coords().latitude());

    String lastTimestamp = (String) redisTemplate.opsForValue()
        .get(CacheUtils.buildTimestampKey(userId))
        .block();

    assertThat(lastTimestamp)
        .isNotNull()
        .isEqualTo(latestLocationRecord.capturedAt().toString());
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void publish_shouldHandleEmptyInput() {
    Set<PublishLocationRequest> emptyRequests = Set.of();

    StepVerifier.create(publishService.publish(emptyRequests))
        .verifyComplete();
  }

  @Test
  public void publish_shouldFailWhenNoUserAuthenticated() {
    Set<PublishLocationRequest> requests = TestLocationUtils.createSampleLocationCoordinates(null);

    StepVerifier.create(publishService.publish(requests))
        .expectError(AuthenticationCredentialsNotFoundException.class)
        .verify();
  }
}
