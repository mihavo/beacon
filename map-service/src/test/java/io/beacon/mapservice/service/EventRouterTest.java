package io.beacon.mapservice.service;

import io.beacon.WithMockBeaconUser;
import io.beacon.config.NoAuthSecurityConfig;
import io.beacon.events.LocationEvent;
import io.beacon.location.RedisTestBase;
import io.beacon.mapservice.clients.AuthGrpcClient;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.router.EventRouter;
import io.beacon.mapservice.utils.TestFriendsUtils;
import io.beacon.mapservice.utils.TestLocationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class EventRouterTest extends RedisTestBase {

    @MockitoBean
    private AuthGrpcClient authGrpcClient;

    @Autowired
    private EventRouter eventRouter;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @WithMockBeaconUser(id = TEST_USER_ID)
    @DisplayName("Should return dispatched records when subscribed to a location event stream")
    @Test
    public void shouldReturnDispatchedRecords_whenSubscribed() {
        UUID clientId = UUID.randomUUID();
        TestFriendsUtils.givenUserIsFriendWith(redisTemplate, TEST_USER_ID, clientId.toString());
        BoundingBox bbox = TestLocationUtils.generateBoundingBox(0.5);
        LocationEvent expectedEvent = new LocationEvent(TEST_USER_ID,
                                                        bbox.minLat() + 0.2,
                                                        bbox.minLon() + 0.2,
                                                        Instant.now());
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            eventRouter.dispatch(expectedEvent).subscribe();
        });
        LocationEvent event = eventRouter.subscribe(clientId.toString(), bbox).blockFirst(Duration.ofSeconds(3));
        assertThat(event).isEqualTo(expectedEvent);
    }

    @WithMockBeaconUser(id = TEST_USER_ID)
    @DisplayName("Should return no location records when user is not in friends list")
    @Test
    public void shouldReturnNoRecords_whenDispatcherNotInFriendsList() {
        UUID clientId = UUID.randomUUID();
        BoundingBox bbox = TestLocationUtils.generateBoundingBox(0.5);
        LocationEvent expectedEvent = new LocationEvent(TEST_USER_ID,
                                                        bbox.minLat() + 0.2,
                                                        bbox.minLon() + 0.2,
                                                        Instant.now());
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            eventRouter.dispatch(expectedEvent).subscribe();
        });
        StepVerifier.create(eventRouter.subscribe(clientId.toString(), bbox)).verifyTimeout(
                Duration.ofSeconds(3));
    }

    @WithMockBeaconUser(id = TEST_USER_ID)
    @DisplayName("Should return no location records when user is in friends list but not subscribed to bbox")
    @Test
    public void shouldReturnNoRecords_whenDispatcherInFriendsListButUserIsNotSubscribedToBbox() {
        UUID clientId = UUID.randomUUID();
        TestFriendsUtils.givenUserIsFriendWith(redisTemplate, TEST_USER_ID, clientId.toString());
        BoundingBox bbox = TestLocationUtils.generateBoundingBox(0.5);
        LocationEvent expectedEvent = new LocationEvent(TEST_USER_ID,
                                                        bbox.minLat() - 0.2,
                                                        bbox.minLon() - 0.2,
                                                        Instant.now());
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            eventRouter.dispatch(expectedEvent).subscribe();
        });
        StepVerifier.create(eventRouter.subscribe(clientId.toString(), bbox)).verifyTimeout(
                Duration.ofSeconds(3));
    }
}
