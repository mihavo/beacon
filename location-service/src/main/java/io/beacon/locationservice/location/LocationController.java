package io.beacon.locationservice.location;

import io.beacon.locationservice.entity.Location;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;

  @GetMapping("/{userId}/recent")
  public ResponseEntity<Flux<Location>> fetchRecent(@PathVariable UUID userId) {
    return ResponseEntity.ok().body(locationService.fetchRecent(userId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getLocation(@PathVariable String id) {
    return ResponseEntity.ok("Location " + id);
  }
}
