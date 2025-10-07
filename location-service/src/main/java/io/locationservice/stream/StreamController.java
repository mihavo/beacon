package io.locationservice.stream;

import io.locationservice.entity.Location;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class StreamController {

  private final StreamService streamService;

  @GetMapping("/{userId}/stream")
  public ResponseEntity<Flux<Location>> stream(@PathVariable UUID userId) {
    //TODO: communicate with user service for permissions to view location
    Flux<Location> stream = streamService.stream(userId);
    return ResponseEntity.ok().body(stream);
  }

}
