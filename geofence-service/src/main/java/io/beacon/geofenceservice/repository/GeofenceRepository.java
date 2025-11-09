package io.beacon.geofenceservice.repository;

import io.beacon.geofenceservice.entity.Geofence;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {
}