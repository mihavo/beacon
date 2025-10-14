package io.locationservice.stream;

import io.locationservice.entity.Location;
import io.locationservice.mappers.LocationMapper;
import io.locationservice.utils.CacheUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class StreamService {

  private final StreamReceiver<String, MapRecord<String, String, Object>> receiver;

  public Flux<Location> stream(UUID userId) {
    String key = CacheUtils.buildLocationStreamKey(userId);
    return receiver.receive(StreamOffset.latest(key)).map(LocationMapper::toLocation);
  }
}
