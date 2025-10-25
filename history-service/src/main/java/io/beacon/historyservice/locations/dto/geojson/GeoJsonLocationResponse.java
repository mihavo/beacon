package io.beacon.historyservice.locations.dto.geojson;

import org.geolatte.geom.G2D;
import org.geolatte.geom.json.GeoJsonFeatureCollection;

public record GeoJsonLocationResponse(
    GeoJsonFeatureCollection<G2D, Void> features
) {
}
