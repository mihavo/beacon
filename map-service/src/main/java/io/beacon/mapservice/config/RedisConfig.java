package io.beacon.mapservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
  @Bean
  @Primary
  public ReactiveStringRedisTemplate defaultRedisTemplate(ReactiveRedisConnectionFactory factory) {
    RedisSerializationContext<String, String> context = RedisSerializationContext.<String, String>newSerializationContext(
            new StringRedisSerializer())
        .value(SerializationPair.fromSerializer(RedisSerializer.string()))
        .build();
    return new ReactiveStringRedisTemplate(factory, context);
  }
}
