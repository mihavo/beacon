package io.beacon.locationservice.publish;

import io.beacon.locationservice.LocationServiceApplicationTests;
import io.beacon.locationservice.config.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestRedisConfiguration.class)
public class PublishLocationServiceTest extends LocationServiceApplicationTests {

  @Test
  public void publish_shouldPublishLocationRecords() {

  }
}
