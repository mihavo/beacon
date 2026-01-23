package io.beacon.mapservice.serivce;

import io.beacon.WithMockBeaconUser;
import io.beacon.events.LocationEvent;
import io.beacon.location.RedisTestBase;
import io.beacon.mapservice.clients.AuthGrpcClient;
import io.beacon.mapservice.mappers.LocationMapper;
import io.beacon.mapservice.mappers.LocationMapperImpl;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.UserLocation;
import io.beacon.mapservice.service.MapService;
import io.beacon.mapservice.utils.TestMapUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.Executors;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@Testcontainers
public class MapServiceTest extends RedisTestBase {


    @Autowired
    private MapService mapService;

    @Autowired
    private final LocationMapper locationMapper;

    @MockitoBean
    private AuthGrpcClient authGrpcClient;

    public MapServiceTest() {
        locationMapper = new LocationMapperImpl();
    }

    @Test
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void shouldReceiveLocationsWhenSubscribed() {
        LocationEvent event = TestMapUtils.createLocationEvent();
        UserLocation expectedLocation = locationMapper.toUserLocation(event);
        Flux<UserLocation> subscribedLocations = mapService.subscribe(TEST_USER_ID,
                                                                      new BoundingBox(event.longitude(),
                                                                                      event.latitude(),
                                                                                      event.longitude() + 5.0,
                                                                                      event.latitude() + 5.0));
        Executors.newSingleThreadExecutor().submit(() -> processLocation(event));
        UserLocation userLocation = subscribedLocations.blockFirst(Duration.ofSeconds(2));
        assertThat(userLocation).isEqualTo(expectedLocation);
    }


    @Test
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void shouldNotReceiveLocationsOutsideOfBoundingBox() {
        LocationEvent event = TestMapUtils.createLocationEvent();
        Flux<UserLocation> subscribedLocations = mapService.subscribe(TEST_USER_ID,
                                                                      new BoundingBox(event.longitude() + 2.0,
                                                                                      event.latitude() + 2.0,
                                                                                      event.longitude() + 5.0,
                                                                                      event.latitude() + 5.0));
        Executors.newSingleThreadExecutor().submit(() -> processLocation(event));
        StepVerifier.create(subscribedLocations).expectTimeout(Duration.ofSeconds(2)).verify();
    }

    private void processLocation(LocationEvent event) {
        try {
            Thread.sleep(200);
            mapService.processLocation(event).block();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
