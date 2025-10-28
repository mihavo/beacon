package io.beacon.mapservice.utils;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;
import ch.hsr.geohash.queries.GeoHashQuery;
import io.beacon.mapservice.models.BoundingBox;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeohashUtils {

  public Set<String> computeGeohashesForBoundingBox(BoundingBox bbox) {
    GeoHashQuery query = getGeoHashQuery(bbox);
    return query.getSearchHashes().stream().map(GeoHash::toBase32).collect(Collectors.toSet());
  }

  public boolean includes(BoundingBox bbox, String geohash) {
    GeoHashQuery query = getGeoHashQuery(bbox);
    return query.contains(GeoHash.fromGeohashString(geohash));
  }

  private static GeoHashQuery getGeoHashQuery(BoundingBox bbox) {
    return new GeoHashBoundingBoxQuery(new ch.hsr.geohash.BoundingBox(bbox.minLat(), bbox.maxLat(), bbox.minLon(),
        bbox.maxLon()));
  }
}
