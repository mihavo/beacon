package io.beacon.mapservice.router;

import io.beacon.mapservice.models.LocationSubscription;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventRouter {

  private final Map<String, LocationSubscription> subscriptions = new ConcurrentHashMap<>();

  public void subscribe() {

  }

  public void unsubscribe() {

  }

  public void dispatch() {
    
  }
}
