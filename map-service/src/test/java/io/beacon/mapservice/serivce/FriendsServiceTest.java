package io.beacon.mapservice.serivce;


import io.beacon.events.FriendshipEvent;
import io.beacon.events.enums.FriendshipEventType;
import io.beacon.location.RedisTestBase;
import io.beacon.mapservice.clients.AuthGrpcClient;
import io.beacon.mapservice.service.FriendsService;
import io.beacon.mapservice.utils.CacheUtils;
import io.beacon.mapservice.utils.TestFriendsUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@EmbeddedKafka
@DirtiesContext
@DisplayName("Friends Service IT Tests")
public class FriendsServiceTest extends RedisTestBase {

    @Autowired
    private FriendsService friendsService;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @MockitoBean
    private AuthGrpcClient authGrpcClient;

    @AfterEach
    void cleanup() {
        redisTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
    }

    @Test
    @DisplayName("Should update list of friends when a FRIEND_ADDED event is received")
    public void shouldUpdateFriendList_whenFriendAddedEventReceived() {
        String friendId = UUID.randomUUID().toString();
        FriendshipEvent event = new FriendshipEvent(FriendshipEventType.FRIEND_ADDED,
                                                    TEST_USER_ID,
                                                    friendId,
                                                    Instant.now());

        StepVerifier.create(friendsService.handleFriendshipEvent(event)).verifyComplete();

        assertEquals(Boolean.TRUE, redisTemplate.opsForSet().isMember(CacheUtils.getFriendshipListKey(TEST_USER_ID),
                                                                      friendId).block());
    }

    @Test
    @DisplayName("Should update list of friends when a FRIEND_REMOVED event is received")
    public void shouldUpdateFriendsList_whenFriendRemoved() {
        String friendId = UUID.randomUUID().toString();
        TestFriendsUtils.givenUserIsFriendWith(redisTemplate, TEST_USER_ID, friendId);
        FriendshipEvent event = new FriendshipEvent(FriendshipEventType.FRIEND_REMOVED,
                                                    TEST_USER_ID,
                                                    friendId,
                                                    Instant.now());
        StepVerifier.create(friendsService.handleFriendshipEvent(event)).verifyComplete();
        assertEquals(Boolean.FALSE, redisTemplate.opsForSet().isMember(CacheUtils.getFriendshipListKey(TEST_USER_ID),
                                                                       friendId).block());

    }

}
