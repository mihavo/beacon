package io.beacon.historyservice.locations.repository;

import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.entity.LocationHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory,
    LocationHistoryId> {
}
