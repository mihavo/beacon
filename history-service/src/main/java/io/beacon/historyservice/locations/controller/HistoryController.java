package io.beacon.historyservice.locations.controller;

import io.beacon.historyservice.locations.entity.LocationHistory;
import io.beacon.historyservice.locations.service.LocationHistoryService;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.QueryParam;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

  private final LocationHistoryService historyService;

  @GetMapping("/recents")
  public Mono<ResponseEntity<Set<LocationHistory>>> fetchRecents(@Positive @QueryParam("limit") Integer limit) {
    return historyService.fetchRecents(limit).map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
  }

  //@GetMapping
  //public Mono<ResponseEntity<Set<LocationHistory>>> fetchBetween(@QueryParam("start")Instant start,
  //    @QueryParam("end") Instant end)
}
