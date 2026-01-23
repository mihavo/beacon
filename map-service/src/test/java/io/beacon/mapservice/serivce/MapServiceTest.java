package io.beacon.mapservice.serivce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
public class MapServiceTest {

    @Test
    public void shouldProcessLocationWhenEventEmitted() {
        
    }
}
