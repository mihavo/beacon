package io.beacon.locationservice.utils;

import com.github.javafaker.Faker;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import locationservice.LocationServiceOuterClass;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestLocationUtils {

  private static final Faker faker = new Faker();

  private static final Random random = new Random();

  public static Set<PublishLocationRequest> createSampleLocationCoordinates(Integer numOfLocations) {
    Integer locationCount = Optional.ofNullable(numOfLocations).orElse(10);
    return IntStream.range(1, locationCount).mapToObj(i -> {
      Coordinates coords =
              new Coordinates((Double.valueOf((faker.address().latitude()).replace(",",
                                                                                   ".")) / 90) * 85, // GEOADD bounds differ from normal latitude bounds https://redis.io/docs/latest/commands/geoadd/
              Double.valueOf(faker.address().longitude().replace(",", ".")));
      return new PublishLocationRequest(coords, Instant.now());
    }).collect(Collectors.toSet());
  }

  public static Set<PublishLocationRequest> createSampleLocationCoordinatesInBbox(LocationServiceOuterClass.BoundingBox bbox,
      Integer numOfLocations) {
    Integer locationCount = Optional.ofNullable(numOfLocations).orElse(10);
    return IntStream.range(1, locationCount).mapToObj(i -> {
      double lat = bbox.getMinLat() + (bbox.getMaxLat() - bbox.getMinLat()) * random.nextDouble();
      double lon = bbox.getMinLon() + (bbox.getMaxLon() - bbox.getMinLon()) * random.nextDouble();
      return new PublishLocationRequest(new Coordinates(lat, lon), Instant.now().plusSeconds(i));
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

  public static LocationServiceOuterClass.BoundingBox generateRandomBoundingBox(double sizeInDegrees) {
    double centerLat = -90 + 180 * random.nextDouble();
    double centerLon = -180 + 360 * random.nextDouble();

    double halfSize = sizeInDegrees / 2;

    return LocationServiceOuterClass.BoundingBox.newBuilder()
        .setMinLon(Math.max(-180, centerLon - halfSize))
        .setMaxLon(Math.min(180, centerLon + halfSize))
        .setMinLat(Math.max(-90, centerLat - halfSize))
        .setMaxLat(Math.min(90, centerLat + halfSize))
        .build();
  }
}
