package io.beacon.historyservice.locations.repository;

import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory,
    LocationHistoryId> {
  
  Set<LocationHistory> findById_UserIdAndId_TimestampBetween(UUID userId, Instant timestampStart, Instant timestampEnd,
      Sort sort);

  @Query(value = """
      SELECT *
      FROM location_history
      WHERE  USER_ID = :userId
      ORDER BY TIMESTAMP DESC
      """, nativeQuery = true)
  Set<LocationHistory> findRecents(@Param("userId") UUID userId, @Param("limit") Limit limit);

  @Query(value = """
        SELECT * FROM location_history
        WHERE ST_DWithin(
          location,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
          :radius
        )
        AND user_id = :userId
      """, nativeQuery = true)
  Set<LocationHistory> findNearby(@Param("userId") UUID userId, @Param("longitude") double longitude,
      @Param("latitude") double latitude,
      @Param("radius") double radius);
}
