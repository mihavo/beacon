package io.beacon.apigateway.config;

import java.util.function.Function;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
      return builder.routes().route("auth-service",
              r -> r.path("/auth/**").filters(getDefaultFilters()).uri("lb://auth-service"))
          .route(
              r -> r.path("/locations/**").filters(getDefaultFilters())
                  .uri("lb://location-service"))
          .route(r -> r.path("/maps/**").filters(getDefaultFilters()).uri("lb://map-service"))
          .route(r -> r.path("/users/**").filters(getDefaultFilters()).uri("lb://user-service"))
          .route(
              r -> r.path("/history/**").filters(getDefaultFilters()).uri("lb://history-service"))
          .route(
              r -> r.path("/geofence/**").filters(getDefaultFilters()).uri("lb://geofence-service"))
          .route(r -> r.path("/notifications/**").filters(getDefaultFilters())
              .uri("lb://notification-service"))
          .build();
    }

  private static Function<GatewayFilterSpec, UriSpec> getDefaultFilters() {
    return f -> f.stripPrefix(1);
  }
}
