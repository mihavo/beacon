package io.beacon.geofenceservice.controller;

import io.beacon.geofenceservice.dto.CreateGeofenceRequest;
import io.beacon.geofenceservice.dto.GeofenceResponse;
import io.beacon.geofenceservice.service.GeofenceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GeofenceController {

  private final GeofenceService geofenceService;

  @PostMapping("/")
  public Mono<ResponseEntity<Void>> create(@Valid @RequestBody CreateGeofenceRequest request) {
    return geofenceService.createGeofence(request).then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).build()));
  }

  @GetMapping("/")
  public Mono<ResponseEntity<List<GeofenceResponse>>> getAllGeofences() {
    return geofenceService.getAllGeofences()
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @PatchMapping("/{geofenceId}/deactivate")
  public Mono<ResponseEntity<Void>> deactivateGeofence(@PathVariable UUID geofenceId) {
    return geofenceService.deactivateGeofence(geofenceId)
        .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
  }

  @PatchMapping("/{geofenceId}/reactivate")
  public Mono<ResponseEntity<Void>> reactivateGeofence(@PathVariable UUID geofenceId) {
    return geofenceService.reactivateGeofence(geofenceId)
        .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
  }

  @DeleteMapping("/{geofenceId}")
  public Mono<ResponseEntity<Void>> deleteGeofence(@PathVariable UUID geofenceId) {
    return geofenceService.deleteGeofence(geofenceId)
        .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build()));
  }
}
