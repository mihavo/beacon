package io.beacon.locationservice.location.fetch;

import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.utils.CacheUtils;
import java.util.UUID;
import locationservice.LocationServiceOuterClass;
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

  /**
   * Fetches recents events from the redis stream of the userId. Does not evaluate permissions.
   *
   * @param userId the key for the redis stream
   * @return a stream of recent locations for the user
   */
  public Flux<Location> fetchRecent(UUID userId) {
    String key = CacheUtils.buildLocationStreamKey(userId);
    return redisTemplate.<String, Object>opsForStream()
        .reverseRange(key, Range.unbounded(), Limit.limit().count(RECENTS_COUNT)).map(
            LocationMapper::toLocation);
  }

  /**
   * Fetches the last known locations for all the currently authenticated user's friends inside a bounding box
   *
   * @param boundingBox the bounding box in which the locations are included
   * @return all the current user's friends' locations inside the bounding box
   */
  public Flux<Location> fetchLKL(LocationServiceOuterClass.BoundingBox boundingBox) {
    
  }

}
