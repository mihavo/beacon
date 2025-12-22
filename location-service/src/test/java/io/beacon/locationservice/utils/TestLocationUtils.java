package io.beacon.locationservice.utils;

import com.github.javafaker.Faker;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.request.PublishLocationRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
}
