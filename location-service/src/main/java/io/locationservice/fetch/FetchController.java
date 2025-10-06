package io.locationservice.fetch;

import io.locationservice.entity.Location;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class FetchController {

  private final FetchService fetchService;

  @GetMapping("/{userId}/recent")
  public Mono<ResponseEntity<Location>> fetchRecent(@PathVariable UUID userId) {
    //TODO: communicate with user service for permissions to view location
    fetchService.fetchRecent(userId);
  }
}
