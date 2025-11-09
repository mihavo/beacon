package io.beacon.geofenceservice.repository;

import io.beacon.geofenceservice.entity.Geofence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {

  List<Geofence> findByUserId(UUID user_id);

  @Modifying
  @Transactional
  @Query("UPDATE Geofence g SET g.isActive = :isActive WHERE g.id = :id")
  int updateIsActive(@Param("id") UUID id, @Param("isActive") boolean isActive);
}