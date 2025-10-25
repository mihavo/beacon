package io.beacon.historyservice.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geolatte.geom.crs.AngularUnit;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.json.GeolatteGeomModule;
import org.geolatte.geom.json.Setting;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> {
      builder.mixIn(AngularUnit.class, AngularUnitMixIn.class);
      GeolatteGeomModule geoModule = new GeolatteGeomModule(CoordinateReferenceSystems.WGS84);
      geoModule.set(Setting.SUPPRESS_CRS_SERIALIZATION, true);
      geoModule.set(Setting.IGNORE_CRS, true);
      builder.modules(geoModule);
      builder.modules(new JavaTimeModule());
    };
  }
}
