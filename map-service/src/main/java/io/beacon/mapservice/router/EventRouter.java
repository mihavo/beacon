package io.beacon.mapservice.router;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.LocationSubscription;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class EventRouter {

  private final Quadtree subsTree = new Quadtree();
  private final Map<String, LocationSubscription> clientSubscriptions = new ConcurrentHashMap<>();

  public Flux<LocationEvent> subscribe(String clientId, BoundingBox boundingBox) {
    Sinks.Many<LocationEvent> sink = Sinks.many().multicast().directBestEffort();
    LocationSubscription subscription = new LocationSubscription(boundingBox, sink);
    clientSubscriptions.put(clientId, subscription);
    log.debug("Subscribed client {} to to bounding box {}", clientId, boundingBox);

    Envelope envelope = new Envelope(boundingBox.minLon(), boundingBox.maxLon(), boundingBox.minLat(), boundingBox.maxLat());
    subsTree.insert(envelope, subscription);
    return sink.asFlux().doFinally(signal -> unsubscribe(clientId));
  }

  public void unsubscribe(String clientId) {
    //TODO: figure out how to unsubscribe in quad trees 
    //LocationSubscription subscription = clientSubscriptions.remove(clientId);
    //log.debug("Unsubscribed client {} from location events", clientId);
  }

  @SuppressWarnings("unchecked")
  public Mono<Void> dispatch(LocationEvent event) {
    return Mono.fromRunnable(() -> {
      List<LocationSubscription> subscriptions =
          subsTree.query(new Envelope(new Coordinate(event.longitude(), event.latitude())));
      subscriptions.forEach(sub -> {
        if (sub.bbox().contains(event.longitude(), event.latitude())) {
          sub.sink().tryEmitNext(event);
        }
      });
    });
  }
}
