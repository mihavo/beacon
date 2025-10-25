package io.beacon.historyservice.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.geolatte.geom.crs.AngularUnit;

public abstract class AngularUnitMixIn {

  @JsonIgnore
  public abstract AngularUnit getFundamentalUnit();
}
