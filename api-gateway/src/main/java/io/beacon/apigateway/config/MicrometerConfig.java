package io.beacon.apigateway.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.lang.NonNullApi;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.reactive.observation.ServerHttpObservationDocumentation;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

@Configuration
@NonNullApi
public class MicrometerConfig {
  private static final String KEY_URI = "uri";
  private static final String UNKNOWN = "UNKNOWN";

  @Bean
  public DefaultServerRequestObservationConvention uriTagContributorForObservationApi() {
    return new DefaultServerRequestObservationConvention() {
      @Override
      public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        KeyValues lowCardinalityKeyValues = super.getLowCardinalityKeyValues(context);
        ServerHttpRequest request = context.getCarrier();

        if (isUriTagNullOrUnknown(context, lowCardinalityKeyValues)) {
          return lowCardinalityKeyValues
              .and(KeyValue.of(KEY_URI, request.getPath().value()));
        }
        return lowCardinalityKeyValues;
      }

      private static boolean isUriTagNullOrUnknown(ServerRequestObservationContext context, KeyValues lowCardinalityKeyValues) {
        Optional<KeyValue> uriKeyValue = lowCardinalityKeyValues.stream()
            .filter(keyValue -> ServerHttpObservationDocumentation.LowCardinalityKeyNames.URI.name()
                .equals(keyValue.getKey()))
            .findFirst();
        return (uriKeyValue.isEmpty() || UNKNOWN.equals(uriKeyValue.get().getValue()));
      }
    };
  }
}
