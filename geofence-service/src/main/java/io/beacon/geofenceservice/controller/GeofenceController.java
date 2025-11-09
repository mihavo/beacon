package io.beacon.geofenceservice.controller;

import io.beacon.geofenceservice.dto.CreateGeofenceRequest;
import io.beacon.geofenceservice.service.GeofenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
