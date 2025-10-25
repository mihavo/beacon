package io.beacon.historyservice.locations.controller;

import io.beacon.historyservice.locations.dto.ClusteredLocation;
import io.beacon.historyservice.locations.dto.LocationHistoryResponse;
import io.beacon.historyservice.locations.service.LocationHistoryService;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

  private final LocationHistoryService historyService;

  @GetMapping("/recents")
  public Mono<ResponseEntity<List<LocationHistoryResponse>>> fetchRecents(
      @Positive @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
    return historyService.fetchRecents(limit).map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @GetMapping("/between")
  public Mono<ResponseEntity<List<LocationHistoryResponse>>> fetchBetween(@RequestParam("start") Instant start,
      @RequestParam("end") Instant end,
      @RequestParam(value = "direction", defaultValue = "DESC") Sort.Direction direction
  ) {
    return historyService.fetchBetween(start, end, direction)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @GetMapping("/nearby")
  public Mono<ResponseEntity<List<LocationHistoryResponse>>> fetchNearby(@RequestParam("latitude") double latitude, @RequestParam(
      "longitude") double longitude, @RequestParam(value = "radius", defaultValue = "500") double radius) {
    return historyService.fetchNearby(latitude, longitude, radius)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @GetMapping("/popular") public Mono<ResponseEntity<List<ClusteredLocation>>> fetchPopular(
      @RequestParam(value = "start", required = false) Instant start,
      @RequestParam(value = "end", required = false) Instant end) {
    return historyService.fetchPopular(Optional.ofNullable(start), Optional.ofNullable(end))
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }
}
