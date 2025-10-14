package io.beacon.locationservice.stream;

import io.beacon.locationservice.entity.Location;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class StreamController {

  private final StreamService streamService;

  @GetMapping(value = "/{userId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Location>> stream(@PathVariable UUID userId) {
    //TODO: communicate with user service for permissions to view location
    Flux<Location> stream = streamService.stream(userId);
    return stream.map(location -> ServerSentEvent.<Location>builder().data(location).build());
  }

}
