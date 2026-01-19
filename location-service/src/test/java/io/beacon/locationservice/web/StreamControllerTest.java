package io.beacon.locationservice.web;

import io.beacon.WithMockBeaconUser;
import io.beacon.locationservice.config.NoAuthSecurityConfig;
import io.beacon.locationservice.config.RedisTestBase;
import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.grpc.clients.AuthGrpcClient;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.publish.PublishService;
import io.beacon.locationservice.request.PublishLocationRequest;
import io.beacon.locationservice.utils.TestAuthUtils;
import io.beacon.locationservice.utils.TestLocationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"location-history-events"})
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class StreamControllerTest extends RedisTestBase {

    private WebTestClient client;

    @MockitoBean
    private AuthGrpcClient authGrpcClient;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PublishService publishService;


    @BeforeEach
    void setup() {
        client = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient().baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void testStream_returnsRecentLocationRecords() throws InterruptedException {
        Set<PublishLocationRequest> locationRequests = TestLocationUtils.createSampleLocationCoordinates(5);
        List<Location> locations = locationRequests.stream().map(LocationMapper::toLocation).toList();


        CountDownLatch sseOpened = new CountDownLatch(1);
        Mono<WebTestClient.ResponseSpec> response = Mono.just(client.get().uri("/" + TEST_USER_ID + "/stream")
                                                                      .header(TestAuthUtils.AUTH_HEADER,
                                                                              TestAuthUtils.createMockAuthHeader())
                                                                      .accept(MediaType.TEXT_EVENT_STREAM).exchange())
                .doOnSubscribe((_) -> {
                    sseOpened.countDown();
                });


        response.subscribe();
        sseOpened.await();
        TestLocationUtils.givenUserHasPublishedLocations(publishService, locationRequests);

        response.doOnNext(res -> {
            List<Location> results = res.expectStatus().isOk().returnResult(Location.class).getResponseBody().collectList().block();
            assertThat(results).containsExactlyInAnyOrderElementsOf(locations);

        }).subscribe();
    }

}
