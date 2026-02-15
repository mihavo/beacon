package io.beacon.mapservice.web;

import io.beacon.WithMockBeaconUser;
import io.beacon.config.NoAuthSecurityConfig;
import io.beacon.events.LocationEvent;
import io.beacon.location.RedisTestBase;
import io.beacon.mapservice.clients.AuthGrpcClient;
import io.beacon.mapservice.utils.TestFriendsUtils;
import io.beacon.mapservice.utils.TestMapUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static io.beacon.TestUserConstants.TEST_USER_ID;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@Import(NoAuthSecurityConfig.class)
@Testcontainers
public class MapControllerTest extends RedisTestBase {

    @MockitoBean
    private AuthGrpcClient authGrpcClient;
    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient client;
   
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    @BeforeEach
    void setup() {
        client = WebTestClient.bindToApplicationContext(applicationContext).configureClient().baseUrl(
                "http://localhost:8080").build();
    }

    @Test
    @DisplayName("Should retrieve list of initial locations of friends")
    @WithMockBeaconUser(id = TEST_USER_ID)
    public void shouldRetrieveInitialLocations() {
        List<String> friendIds = IntStream.range(0, 5).mapToObj(_ -> UUID.randomUUID().toString()).toList();
        friendIds.forEach(friendId -> TestFriendsUtils.givenUserIsFriendWith(redisTemplate, TEST_USER_ID, friendId));
        friendIds.forEach(friendId -> {
            LocationEvent event = TestMapUtils.createLocationEvent();
        });

    }


}
