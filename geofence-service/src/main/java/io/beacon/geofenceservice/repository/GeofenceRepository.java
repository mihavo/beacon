package io.beacon.geofenceservice.repository;

import io.beacon.geofenceservice.entity.Geofence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {

  List<Geofence> getGeofencesByUser_id(UUID userId);
}