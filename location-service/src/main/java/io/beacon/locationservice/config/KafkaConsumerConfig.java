package io.locationservice.config;

import io.locationservice.authz.events.models.FriendshipEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;
  
  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Bean
  public ConsumerFactory<String, FriendshipEvent> friendshipEventsConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    JsonDeserializer<FriendshipEvent> deserializer = new JsonDeserializer<>(FriendshipEvent.class);
    deserializer.addTrustedPackages("io.*");
    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
        deserializer);
  }


  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, FriendshipEvent> friendshipKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, FriendshipEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(friendshipEventsConsumerFactory());
    return factory;
  }

}
