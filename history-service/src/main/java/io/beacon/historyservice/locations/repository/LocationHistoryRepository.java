package io.beacon.historyservice.locations.repository;

import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory,
    LocationHistoryId> {

  Set<LocationHistory> findById_UserIdAndId_TimestampBetween(UUID userId, Instant timestampStart, Instant timestampEnd);

  @Query(value = """
      SELECT *
      FROM LOCATION_HISTORY
      WHERE  USER_ID = :userId
      ORDER BY TIMESTAMP DESC
      LIMIT  :limit
      """, nativeQuery = true)
  Set<LocationHistory> findRecents(@Param("userI") UUID userId, @Param("limit") Limit limit);
}
