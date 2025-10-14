package io.beacon.locationservice.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.data.redis.stream.StreamReceiver.StreamReceiverOptions;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> jsonRedisTemplate(
        ReactiveRedisConnectionFactory factory) {
        var keySerializer = new StringRedisSerializer();
        var valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, Object> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

  @Bean
  @Primary
  public ReactiveStringRedisTemplate defaultRedisTemplate(ReactiveRedisConnectionFactory factory) {
    RedisSerializationContext<String, String> context = RedisSerializationContext.<String, String>newSerializationContext(
            new StringRedisSerializer())
        .value(SerializationPair.fromSerializer(RedisSerializer.string()))
        .build();
    return new ReactiveStringRedisTemplate(factory, context);
  }

  @Bean
  public StreamReceiver<String, MapRecord<String, String, Object>> locationStreamReceiver(
      ReactiveRedisConnectionFactory factory) {
    StreamReceiverOptions<String, MapRecord<String, String, Object>> options = StreamReceiverOptions.builder()
        .pollTimeout(Duration.ofMillis(100))
        .hashValueSerializer(
            SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
        .hashKeySerializer(SerializationPair.fromSerializer(new StringRedisSerializer()))
        .build();

    return StreamReceiver.create(factory, options);
  }


}
