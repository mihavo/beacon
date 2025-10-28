package io.beacon.mapservice.models;

public record BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {

  public boolean contains(double lon, double lat) {
    return lon >= minLon && lon <= maxLon &&
        lat >= minLat && lat <= maxLat;
  }
}
