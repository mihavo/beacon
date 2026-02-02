package io.beacon.locationservice.location.controller;

import io.beacon.locationservice.entity.Location;
import io.beacon.locationservice.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;

  @GetMapping("/{userId}/recents")
  public ResponseEntity<Flux<Location>> fetchRecent(@PathVariable UUID userId) {
    return ResponseEntity.ok().body(locationService.fetchRecent(userId));
  }

}
