package io.beacon.locationservice.web;

import io.beacon.WithMockBeaconUser;
import io.beacon.config.NoAuthSecurityConfig;
import io.beacon.location.RedisTestBase;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestAuthUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class LocationControllerTest extends RedisTestBase {

    private WebTestClient client;

    @MockitoBean
    private AuthGrpcClient authGrpcClient;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PublishService publishService;

    @BeforeEach
    void setup() {
        client = WebTestClient.bindToApplicationContext(applicationContext).configureClient().baseUrl(
                "http://localhost:8080").build();
    }

    @Test
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void testFetchRecent_returnsRecentLocations() {
        Set<PublishLocationRequest> locationRequests = TestLocationUtils.createSampleLocationCoordinates(5);
        TestLocationUtils.givenUserHasPublishedLocations(publishService, locationRequests);

        client.get().uri("/" + TEST_USER_ID + "/recents")
                .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
                .exchange()
                .expectStatus().isOk().expectBodyList(Location.class).consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    List<Location> locations
                            = locationRequests.stream().map(LocationMapper::toLocation).toList();
                    assertThat(response.getResponseBody()).containsExactlyInAnyOrderElementsOf(locations
                    );
                });
    }

    @Test
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void testFetchRecent_noLocations() {

        client.get().uri("/" + TEST_USER_ID + "/recents")
                .header(TestAuthUtils.AUTH_HEADER, TestAuthUtils.createMockAuthHeader())
                .exchange()
                .expectStatus().isOk().expectBodyList(Location.class).consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    assertThat(response.getResponseBody()).isEmpty();
                });
    }
}
