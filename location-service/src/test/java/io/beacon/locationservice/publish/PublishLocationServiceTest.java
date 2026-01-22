package io.beacon.locationservice.publish;

import io.beacon.WithMockBeaconUser;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.CacheUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.stream.Collectors;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
@Testcontainers
@EmbeddedKafka
@DirtiesContext
public class PublishLocationServiceTest extends RedisTestBase {

  @Autowired private PublishService publishService;
  @Autowired private ReactiveRedisTemplate<String, Object> redisTemplate;
  @Autowired private ReactiveRedisTemplate<String, String> valueRedisTemplate;

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

  @AfterEach
  void cleanup() {
    redisTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
  }

  @Test
  @WithMockBeaconUser(id = TEST_USER_ID)
  public void publish_shouldPublishLocationRecords() {
    Set<PublishLocationRequest> request = TestLocationUtils.createSampleLocationCoordinates(null);
    StepVerifier.create(publishService.publish(request)).expectNextCount(request.size()).verifyComplete();

    UUID userId = UUID.fromString(TEST_USER_ID);
    String cacheLocationKey = CacheUtils.buildLocationStreamKey(userId);
    List<MapRecord<String, Object, Object>> streamRecords =
        redisTemplate.opsForStream()
            .range(cacheLocationKey, Range.unbounded())
            .collectList()
            .block();

    assertNotNull(streamRecords);
    assertEquals(streamRecords.size(), request.size());

    Set<Pair<Double, Double>>
        requestCoords = request.stream()
        .map(record -> Pair.of(record.coords().latitude(), record.coords().longitude()))
        .collect(Collectors.toSet());

    Set<Pair<Double, Double>> resultCoords = streamRecords.stream().map(record -> {
      Map<Object, Object> coords = (record).getValue();
      return Pair.of(Double.valueOf((String) coords.get("lat")), Double.valueOf((String) coords.get("lon")));
    }).collect(Collectors.toSet());

    assertThat(resultCoords).isEqualTo(requestCoords);

    PublishLocationRequest latestLocationRecord =
        request.stream().max(Comparator.comparing(PublishLocationRequest::capturedAt)).orElseThrow();
    Point savedPoint = valueRedisTemplate.opsForGeo()
        .position(CacheUtils.getLocationGeospatialKey(),
            CacheUtils.buildGeospatialMember(userId))
        .block();

    assertThat(savedPoint).isNotNull();
    assertThat(savedPoint.getX()).isCloseTo(latestLocationRecord.coords().longitude(), within(0.0001));
    assertThat(savedPoint.getY()).isCloseTo(latestLocationRecord.coords().latitude(), within(0.0001));

    String lastTimestamp = valueRedisTemplate.opsForValue()
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
        .expectError(NoSuchElementException.class)
        .verify();
  }

  @Test
  public void publish_shouldFailWhenNoUserAuthenticated() {
    Set<PublishLocationRequest> requests = TestLocationUtils.createSampleLocationCoordinates(null);

    StepVerifier.create(publishService.publish(requests))
        .expectError(AuthenticationCredentialsNotFoundException.class)
        .verify();
  }
}
