package io.locationservice.fetch;

import io.locationservice.entity.Location;
import io.locationservice.mappers.LocationMapper;
import io.locationservice.utils.CacheUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class FetchService {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;

  private static final Integer RECENTS_COUNT = 10;

  public Flux<Location> fetchRecent(UUID userId) {
    String key = CacheUtils.buildLocationStreamKey(userId);
    return redisTemplate.<String, Object>opsForStream()
        .reverseRange(key, Range.unbounded(), Limit.limit().count(RECENTS_COUNT)).map(
            LocationMapper::toLocation);
  }

}
