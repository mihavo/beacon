package io.locationservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController {

  @GetMapping("/{id}")
  public ResponseEntity<String> getLocation(@PathVariable String id) {
    return ResponseEntity.ok("Location " + id);
  }
}
