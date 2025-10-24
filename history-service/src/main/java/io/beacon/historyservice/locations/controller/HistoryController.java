package io.beacon.historyservice.locations.controller;

import io.beacon.historyservice.locations.dto.LocationHistoryResponse;
import io.beacon.historyservice.locations.service.LocationHistoryService;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.Set;
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
  public Mono<ResponseEntity<Set<LocationHistoryResponse>>> fetchRecents(
      @Positive @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
    return historyService.fetchRecents(limit).map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  @GetMapping
  public Mono<ResponseEntity<Set<LocationHistoryResponse>>> fetchBetween(@RequestParam("start") Instant start,
      @RequestParam("end") Instant end,
      @RequestParam(value = "direction", defaultValue = "DESC") Sort.Direction direction
  ) {
    return historyService.fetchBetween(start, end, direction)
        .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }
}
