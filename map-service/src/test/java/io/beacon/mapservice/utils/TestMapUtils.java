package io.beacon.mapservice.utils;

import io.beacon.events.LocationEvent;
import io.beacon.location.TestLocationUtils;

import java.time.Instant;

import static io.beacon.TestUserConstants.TEST_USER_ID;

public class TestMapUtils {

    public static LocationEvent createLocationEvent() {
        return new LocationEvent(TEST_USER_ID,
                                 TestLocationUtils.generateRandomLatitude(),
                                 TestLocationUtils.generateRandomLongitude(),
                                 Instant.now());
    }
}
