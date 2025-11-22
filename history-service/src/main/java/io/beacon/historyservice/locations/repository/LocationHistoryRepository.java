package io.beacon.historyservice.locations.repository;

import io.beacon.historyservice.locations.dto.ClusteredLocation;
import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import java.time.Instant;
import java.util.List;
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

  @Query(value = """
      SELECT cluster_id,
             ST_X(ST_Centroid(ST_Collect(location::geometry))) AS longitude,
             ST_Y(ST_Centroid(ST_Collect(location::geometry))) AS latitude,
             COUNT(*) AS visits
      FROM (
          SELECT ST_ClusterKMeans(location::geometry, 5) OVER () AS cluster_id, location
        FROM location_history
        WHERE user_id = :userId
            AND location IS NOT NULL
            AND (CAST(:start AS timestamptz) IS NULL OR timestamp >= CAST(:start AS timestamptz))
            AND (CAST(:end AS timestamptz) IS NULL OR timestamp <= CAST(:end AS timestamptz))
      ) AS clusters
      GROUP BY cluster_id
      HAVING ST_X(ST_Centroid(ST_Collect(location::geometry))) IS NOT NULL
         AND ST_Y(ST_Centroid(ST_Collect(location::geometry))) IS NOT NULL
      ORDER BY visits DESC;
      """, nativeQuery = true)
  List<ClusteredLocation> findPopular(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end);
}
