package io.beacon.mapservice.models;

import io.beacon.events.LocationEvent;
import reactor.core.publisher.Sinks;

public record LocationSubscription(String clientId, BoundingBox bbox, Sinks.Many<LocationEvent> sink) {
}
