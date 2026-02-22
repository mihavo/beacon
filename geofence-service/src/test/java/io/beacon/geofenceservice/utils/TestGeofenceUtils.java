package io.beacon.geofenceservice.utils;

import io.beacon.events.enums.TriggerType;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import io.beacon.location.TestLocationUtils;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Optional;
import java.util.UUID;

import static io.beacon.TestUserConstants.TEST_USER_ID;

public class TestGeofenceUtils {

    private static final GeometryFactory GEO = new GeometryFactory();

    public static void givenSampleGeofence(GeofenceRepository repository, UUID targetId, TriggerType triggerType) {
        Point point = GEO.createPoint(new CoordinateXY(TestLocationUtils.generateRandomLongitude(),
                                                       TestLocationUtils.generateRandomLatitude()));
        point.setSRID(4326);
        Geofence geofence = Geofence.builder().center(point)
                .radius_meters(500.0)
                .userId(UUID.fromString(TEST_USER_ID))
                .targetId(Optional.ofNullable(targetId).orElse(UUID.randomUUID()))
                .triggerType(Optional.ofNullable(triggerType).orElse(triggerType))
                .build();

        repository.save(geofence);
    }
}
