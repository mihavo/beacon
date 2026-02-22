package io.beacon.geofenceservice.service;

import io.beacon.WithMockBeaconUser;
import io.beacon.events.enums.TriggerType;
import io.beacon.geofenceservice.clients.AuthGrpcClient;
import io.beacon.geofenceservice.dto.CreateGeofenceRequest;
import io.beacon.geofenceservice.dto.CreateGeofenceResponse;
import io.beacon.geofenceservice.dto.GeofenceResponse;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import io.beacon.geofenceservice.utils.TestGeofenceUtils;
import io.beacon.location.TestLocationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static io.beacon.TestUserConstants.TEST_USER_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EmbeddedKafka
@DirtiesContext
public class GeofenceServiceTest {

    @MockitoBean
    private AuthGrpcClient authGrpcClient;

    @Autowired
    private GeofenceService geofenceService;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @DisplayName("Should create a new geofence object when a geofence request comes in")
    @WithMockBeaconUser(id = TEST_USER_ID)
    @Test
    public void shouldCreateGeofence_whenRequested() {
        CreateGeofenceRequest request = new CreateGeofenceRequest(TEST_USER_ID,
                                                                  TestLocationUtils.generateRandomLongitude(),
                                                                  TestLocationUtils.generateRandomLatitude(),
                                                                  5000.0,
                                                                  TriggerType.ENTER);

        CreateGeofenceResponse response = geofenceService.createGeofence(request).block();

        Geofence actual = geofenceRepository.getGeofenceById(response.geofence_id());
        assertThat(actual.getRadius_meters()).isEqualTo(request.radiusMeters());
        assertThat(actual.getUserId().toString()).isEqualTo(TEST_USER_ID);
        assertThat(actual.getTriggerType()).isEqualTo(TriggerType.ENTER);
        assertThat(actual.getCenter().getX()).isEqualTo(request.centerLongitude());
        assertThat(actual.getCenter().getY()).isEqualTo(request.centerLatitude());
    }

    @DisplayName("Should return all created geofences when requested")
    @WithMockBeaconUser(id = TEST_USER_ID)
    @Test
    public void shouldReturnAllGeofences_whenRequested() {
        UUID targetId = UUID.randomUUID();
        TestGeofenceUtils.givenSampleGeofence(geofenceRepository, targetId, TriggerType.ENTER);
        TestGeofenceUtils.givenSampleGeofence(geofenceRepository, targetId, TriggerType.NEAR);

        List<GeofenceResponse> responses = geofenceService.getAllGeofences().block();
        assertEquals(2, responses.size());
        assertEquals(TriggerType.ENTER, responses.get(0).triggerType());
        assertEquals(TriggerType.NEAR, responses.get(1).triggerType());
    }
}
