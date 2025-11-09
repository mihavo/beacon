package io.beacon.geofenceservice.service;

import io.beacon.geofenceservice.dto.CreateGeofenceRequest;
import io.beacon.geofenceservice.dto.CreateGeofenceResponse;
import io.beacon.geofenceservice.dto.GeofenceResponse;
import io.beacon.geofenceservice.entity.Geofence;
import io.beacon.geofenceservice.enums.TriggerType;
import io.beacon.geofenceservice.mappers.GeofenceMapper;
import io.beacon.geofenceservice.repository.GeofenceRepository;
import io.beacon.security.utils.AuthUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class GeofenceService {

  private final GeofenceRepository geofenceRepository;
  private final GeofenceMapper geofenceMapper;
  private static final GeometryFactory GEO = new GeometryFactory();

  public Mono<CreateGeofenceResponse> createGeofence(CreateGeofenceRequest request) {
    return AuthUtils.getCurrentUserId()
        .flatMap(userId -> Mono.fromCallable(() -> {
          Point point = GEO.createPoint(new CoordinateXY(request.centerLongitude(), request.centerLatitude()));
          point.setSRID(4326);
          Geofence geofence = Geofence.builder()
              .center(point)
              .radius_meters(request.radius_meters())
              .userId(userId)
              .targetId(UUID.fromString(request.userId()))
              .triggerType(Optional.ofNullable(request.triggerType()).orElse(TriggerType.ENTER))
              .build();

          return geofenceMapper.toCreateResponse(geofenceRepository.save(geofence));
        }).subscribeOn(Schedulers.boundedElastic()));
  }

  public Mono<List<GeofenceResponse>> getAllGeofences() {
    return AuthUtils.getCurrentUserId().flatMap(userId ->
        Mono.fromCallable(() -> geofenceRepository.findByUserId(userId)
            .stream()
            .map(geofenceMapper::toResponse)
            .toList()).subscribeOn(Schedulers.boundedElastic())
    );
  }

  public Mono<Void> deactivateGeofence(UUID geofenceId) {
    return updateActiveStatus(geofenceId, false);
  }

  public Mono<Void> reactivateGeofence(UUID geofenceId) {
    return updateActiveStatus(geofenceId, true);
  }

  public Mono<Void> deleteGeofence(UUID geofenceId) {
    return checkOwnership(geofenceId).then(Mono.fromRunnable(() -> geofenceRepository.deleteById(geofenceId))
        .subscribeOn(Schedulers.boundedElastic())).then();
  }

  private Mono<Void> updateActiveStatus(UUID geofenceId, boolean isActive) {
    return checkOwnership(geofenceId)
        .then(Mono.fromCallable(() -> geofenceRepository.updateIsActive(geofenceId, isActive))
            .subscribeOn(Schedulers.boundedElastic()))
        .then();
  }

  private Mono<Void> checkOwnership(UUID geofenceId) {
    Geofence geofence = geofenceRepository.findById(geofenceId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Geofence " + geofenceId + " not found."));
    return AuthUtils.getCurrentUserId().flatMap(userId -> {
      if (!userId.equals(geofence.getUserId())) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
      }
      return Mono.empty();
    });
  }
}
