package io.beacon.mapservice.utils;

import io.beacon.mapservice.models.BoundingBox;

import java.util.Random;

public class TestLocationUtils {

    private static Random random = new Random();

    public static BoundingBox generateBoundingBox(double sizeInDegrees) {
        double centerLat = -90 + 180 * random.nextDouble();
        double centerLon = -180 + 360 * random.nextDouble();

        double halfSize = sizeInDegrees / 2;

        return new BoundingBox(
                Math.max(-180, centerLon - halfSize),
                Math.max(-90, centerLat - halfSize),
                Math.min(180, centerLon + halfSize),
                Math.min(90, centerLat + halfSize)
        );
    }
}
