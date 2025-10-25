package io.beacon.historyservice.locations.mappers;

import io.beacon.historyservice.locations.entity.LocationHistory;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.geolatte.geom.Feature;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.GeoJsonFeatureCollection;

public class GeoJsonMapper {

  public static GeoJsonFeatureCollection<G2D, Void> toFeatureCollection(Set<LocationHistory> locations) {
    List<Feature<G2D, Void>> features = locations.stream().map(GeoJsonMapper::toFeature).collect(Collectors.toList());
    return new GeoJsonFeatureCollection<>(features);
  }

  public static GeoJsonFeature<G2D, Void> toFeature(LocationHistory location) {
    Point<G2D> point = new Point<>(new G2D(location.getLongitude(), location.getLatitude()), CoordinateReferenceSystems.WGS84);
    HashMap<String, Object> props = new HashMap<>();
    props.put("timestamp", location.getId().getTimestamp());
    return new GeoJsonFeature<>(point, null, props
    );
  }
}
