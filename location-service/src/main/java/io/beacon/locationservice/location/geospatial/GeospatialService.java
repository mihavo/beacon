package io.beacon.locationservice.location.geospatial;

import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.models.Coordinates;
import io.beacon.locationservice.utils.CacheUtils;
import locationservice.LocationServiceOuterClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeospatialService {

  private final ReactiveRedisTemplate<String, Object> redisTemplate;

  public Flux<Location> searchInBoundingBox(LocationServiceOuterClass.BoundingBox boundingBox,
      Coordinates coords) {
    double minLat = boundingBox.getMinLat();
    double maxLat = boundingBox.getMaxLat();
    double minLon = boundingBox.getMinLon();
    double maxLon = boundingBox.getMaxLon();

    double centerLat = (minLat + maxLat) / 2.0;
    double centerLon = (minLon + maxLon) / 2.0;

    double widthDeg = maxLon - minLon;
    double heightDeg = maxLat - minLat;
    double widthKm = DistanceUtils.degrees2Dist(widthDeg, DistanceUtils.EARTH_MEAN_RADIUS_KM);
    double heightKm = DistanceUtils.degrees2Dist(heightDeg, DistanceUtils.EARTH_MEAN_RADIUS_KM);

    return redisTemplate.opsForGeo().search(
        CacheUtils.getLocationGeospatialKey(),
        GeoReference.fromCoordinate(new Point(centerLon, centerLat)),
        GeoShape.byBox(widthKm, heightKm, RedisGeoCommands.DistanceUnit.KILOMETERS),
        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
    ).flatMap(res -> {
      res.getContent().get
    });
  }
}
