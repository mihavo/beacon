package io.beacon.geofenceservice.service;

import io.beacon.geofenceservice.dto.CreateGeofenceRequest;
import io.beacon.geofenceservice.dto.CreateGeofenceResponse;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.mappers.GeofenceMapper;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class GeofenceService {

  private final GeofenceRepository geofenceRepository;
  private final GeofenceMapper geofenceMapper;

  public Mono<CreateGeofenceResponse> createGeofence(CreateGeofenceRequest request) {
    return Mono.fromCallable(() -> {
      Geofence geofence = Geofence.builder()
          .center(request.center())
          .radius_meters(request.radius_meters())
          .user_id(UUID.fromString(request.userId()))
          .build();

      return geofenceMapper.toResponse(geofenceRepository.save(geofence));
    }).subscribeOn(Schedulers.boundedElastic());
  }
}
