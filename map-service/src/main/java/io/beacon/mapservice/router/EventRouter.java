package io.beacon.mapservice.router;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.LocationSubscription;
import io.beacon.mapservice.service.FriendsService;
import io.beacon.permissions.FriendshipAction;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventRouter {

  private final Quadtree subsTree = new Quadtree();
  private final Map<String, LocationSubscription> clientSubscriptions = new ConcurrentHashMap<>();
  private final FriendsService friendsService;

  public Flux<LocationEvent> subscribe(String clientId, BoundingBox boundingBox) {
    Sinks.Many<LocationEvent> sink = Sinks.many().multicast().directBestEffort();
    LocationSubscription subscription = new LocationSubscription(boundingBox, clientId, sink);
    clientSubscriptions.put(clientId, subscription);
    log.debug("Subscribed client {} to to bounding box {}", clientId, boundingBox);

    Envelope envelope = new Envelope(boundingBox.minLon(), boundingBox.maxLon(), boundingBox.minLat(), boundingBox.maxLat());
    subsTree.insert(envelope, subscription);
    return sink.asFlux().doFinally(signal -> unsubscribe(envelope, clientId));
  }

  public void unsubscribe(Envelope envelope, String clientId) {
    LocationSubscription subscription = clientSubscriptions.remove(clientId);
    subsTree.remove(envelope, subscription);
    log.debug("Unsubscribed client {} from location events", clientId);
  }

  @SuppressWarnings("unchecked")
  public Mono<Void> dispatch(LocationEvent event) {
      List<LocationSubscription> subscriptions =
          subsTree.query(new Envelope(new Coordinate(event.longitude(), event.latitude())));
    return Flux.fromIterable(subscriptions)
        .flatMap(sub -> {
          boolean isContained = sub.bbox().contains(event.longitude(), event.latitude());
          if (!isContained) {
            return Mono.empty();
          }
          return friendsService.canPerform(UUID.fromString(sub.clientId()), UUID.fromString(event.userId()),
                  FriendshipAction.VIEW_LOCATION)
              .filter(Boolean::booleanValue)
              .doOnNext(_ -> sub.sink().tryEmitNext(event));
        })
        .then();
  }
}
