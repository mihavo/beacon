package io.beacon.historyservice.locations.repository;

import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Point;
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
      FROM LOCATION_HISTORY
      WHERE  USER_ID = :userId
      ORDER BY TIMESTAMP DESC
      """, nativeQuery = true)
  Set<LocationHistory> findRecents(@Param("userId") UUID userId, @Param("limit") Limit limit);

  @Query("""
      SELECT l
      FROM LocationHistory l
      WHERE dwithin(l.location, : center, :radius) = true
      """)
  Set<LocationHistory> findNearby(@Param("center") Point center, @Param("radius") double radius);
}
