package io.beacon.mapservice.utils;

import io.beacon.mapservice.models.BoundingBox;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeohashUtils {

  public Set<String> computeGeohashesForBoundingBox(BoundingBox bbox, int precision) {
    return com.github.davidmoten.geo.GeoHash.coverBoundingBox(bbox.maxLat(), bbox.minLon(), bbox.minLat(), bbox.maxLon(),
            precision)
        .getHashes();
  }
}
