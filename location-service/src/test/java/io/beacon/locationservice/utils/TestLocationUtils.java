package io.beacon.locationservice.utils;

import com.github.javafaker.Faker;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

public class TestLocationUtils {

  private static final Faker faker = new Faker();

  public static Set<PublishLocationRequest> createSampleLocationCoordinates(Integer numOfLocations) {
    Integer locationCount = Optional.ofNullable(numOfLocations).orElse(10);
    return IntStream.range(1, locationCount).mapToObj(i -> {
      Coordinates coords =
          new Coordinates(Double.valueOf(faker.address().latitude().replace(",", ".")),
              Double.valueOf(faker.address().longitude().replace(",", ".")));
      return new PublishLocationRequest(coords, Instant.now());
    }).collect(Collectors.toSet());
  }

  public static void givenUserHasPublishedLocations(PublishService publishService,
      Set<PublishLocationRequest> locationRequests) {
    publishService.publish(locationRequests).blockLast();
  }

  public static void givenUserHasPublishedLocationsDirectly(ReactiveRedisTemplate<String, Object> redisTemplate,
      String userId, Set<PublishLocationRequest> locationRequests) {
    String streamKey = CacheUtils.buildLocationStreamKey(UUID.fromString(userId));
    for (PublishLocationRequest location : locationRequests) {
      Map<String, Object> fields = Map.of(
          "lat", location.coords().latitude(),
          "lon", location.coords().longitude(),
          "capturedAt", location.capturedAt()
      );

      MapRecord<String, String, Object> record =
          StreamRecords.newRecord().in(streamKey).ofMap(fields);

      redisTemplate.opsForStream().add(record).block();
    }
  }

  public static void givenUsersAreInFriendsList(ReactiveRedisTemplate<String, Object> redisTemplate, String userId,
      String... friendIds) {
    String key = CacheUtils.getFriendshipListKey(userId);
    redisTemplate.opsForSet().add(key, friendIds).block();
  }
}
