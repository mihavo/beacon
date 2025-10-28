package io.beacon.mapservice.router;

import io.beacon.events.LocationEvent;
import io.beacon.mapservice.models.BoundingBox;
import io.beacon.mapservice.models.LocationSubscription;
import io.beacon.mapservice.utils.GeohashUtils;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class EventRouter {

  private final Map<String, Set<LocationSubscription>> geohashSubscriptions = new ConcurrentHashMap<>();
  private final Map<String, LocationSubscription> clientSubscriptions = new ConcurrentHashMap<>();

  public Flux<LocationEvent> subscribe(String clientId, BoundingBox boundingBox) {
    Sinks.Many<LocationEvent> sink = Sinks.many().multicast().directBestEffort();
    LocationSubscription subscription = new LocationSubscription(boundingBox, sink);
    clientSubscriptions.put(clientId, subscription);
    log.debug("Subscribed client {} to to bounding box {}", clientId, boundingBox);

    Set<String> geohashes = GeohashUtils.computeGeohashesForBoundingBox(boundingBox);
    geohashes.forEach(
        geohash -> geohashSubscriptions.computeIfAbsent(geohash, k -> ConcurrentHashMap.newKeySet()).add(subscription));
    log.debug("Generated geohash subscriptions for client {}", clientId);
    return sink.asFlux().doFinally(signal -> unsubscribe(clientId));
  }

  public void unsubscribe(String clientId) {
    LocationSubscription subscription = clientSubscriptions.remove(clientId);
    if (subscription != null) {
      geohashSubscriptions.values().forEach(set -> set.remove(subscription));
    }
    log.debug("Unsubscribed client {} from location events", clientId);
  }

  public Mono<Void> dispatch(LocationEvent event) {
    return Mono.fromRunnable(() -> {
      String geohash = org.locationtech.spatial4j.io.GeohashUtils.encodeLatLon(event.longitude(), event.latitude());
      Set<LocationSubscription> subscriptions = geohashSubscriptions.get(geohash);
      subscriptions.forEach(sub -> {
        if (sub.bbox().contains(event.longitude(), event.latitude())) {
          sub.sink().tryEmitNext(event);
        }
      });
    });
  }
}
