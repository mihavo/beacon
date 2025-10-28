package io.beacon.mapservice.controller;

import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.UserLocation;
import io.beacon.mapservice.service.MapService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class MapController {

  private final MapService mapService;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<UserLocation>> stream(@RequestParam("minLat") double minLat, @RequestParam("maxLat") double maxLat,
      @RequestParam("minLon") double minLon, @RequestParam("maxLon") double maxLon) {
    BoundingBox boundingBox = new BoundingBox(minLon, minLat, maxLon, maxLat);
    return mapService.subscribe(UUID.randomUUID().toString(), boundingBox)
        .map(location -> ServerSentEvent.<UserLocation>builder().data(location).build());
  }

  @GetMapping("/snapshot")
  public Flux<UserLocation> initialLocations(
      @RequestParam double minLat,
      @RequestParam double maxLat,
      @RequestParam double minLon,
      @RequestParam double maxLon) {
    BoundingBox boundingBox = new BoundingBox(minLat, maxLat, minLon, maxLon);
    return mapService.getCurrentLocations(boundingBox);
  }
}
