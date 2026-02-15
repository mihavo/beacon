package io.beacon.mapservice.utils;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

public class TestFriendsUtils {

    public static void givenUserIsFriendWith(ReactiveStringRedisTemplate redisTemplate, String selfId,
                                             String friendId) {
        redisTemplate.opsForSet().add(CacheUtils.getFriendshipListKey(selfId), friendId).block();
        redisTemplate.opsForSet().add(CacheUtils.getFriendshipListKey(friendId), selfId).block();
    }
}
