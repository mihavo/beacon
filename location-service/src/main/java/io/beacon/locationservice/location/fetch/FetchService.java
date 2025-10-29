package io.beacon.locationservice.location.fetch;

import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.grpc.clients.UserGrpcClient;
import io.beacon.locationservice.location.geospatial.GeospatialService;
import io.beacon.locationservice.mappers.LocationMapper;
import io.beacon.locationservice.models.UserInfo;
import io.beacon.locationservice.models.UserLocation;
import io.beacon.locationservice.utils.CacheUtils;
import java.util.List;
import java.util.UUID;
import locationservice.LocationServiceOuterClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j @Service
@RequiredArgsConstructor
public class FetchService {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;

  private static final Integer RECENTS_COUNT = 10;
  private final UserGrpcClient userGrpcClient;
  private final GeospatialService geospatialService;

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
            LocationMapper::toUserLocation);
  }

  /**
   * Fetches the last known locations for all the currently authenticated user's friends inside a bounding box
   *
   * @param boundingBox the bounding box in which the locations are included
   * @param userId the user id of the requester (owner of friends' relations) 
   * @return all the current user's friends' locations inside the bounding box
   */
  public Flux<UserLocation> fetchLKL(LocationServiceOuterClass.BoundingBox boundingBox, UUID userId) {
    return Flux.fromIterable(userGrpcClient.getUserFriends(userId.toString())
    ).collectList().flatMapMany(friends -> {
      List<String> friendIds = friends.stream().map(UserInfo::userId).toList();
      return geospatialService.searchInBoundingBox(boundingBox).filter(location -> friendIds.contains(location.userId()));
    }).doOnComplete(() -> log.debug("Fetched last known locations for bounding box {}", boundingBox));
  }

}
