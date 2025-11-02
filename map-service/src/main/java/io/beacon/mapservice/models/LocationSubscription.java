package io.beacon.mapservice.models;

import io.beacon.events.LocationEvent;
import reactor.core.publisher.Sinks;

public record LocationSubscription(BoundingBox bbox, String clientId, Sinks.Many<LocationEvent> sink) {
}
