package io.beacon.userservice.config;

import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;

@Configuration
@EnableNeo4jAuditing(
    modifyOnCreate = false,
    auditorAwareRef = "auditorProvider",
    dateTimeProviderRef = "fixedDateTimeProvider"
)
public class AuditConfig {

  static final Instant DEFAULT_CREATION_AND_MODIFICATION_DATE = Instant.parse(
      "2019-03-07T11:18:34.00Z");

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional.of("A user");
  }

  @Bean
  public DateTimeProvider fixedDateTimeProvider() {
    return () -> Optional.of(DEFAULT_CREATION_AND_MODIFICATION_DATE);
  }
}