package io.beacon.userservice.connections.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ConnectionsInfo(List<UserInfo> connections) {

  public ConnectionsInfo {
    connections = connections == null ? List.of() : List.copyOf(connections);
  }

  @JsonProperty("numOfConnections")
  public int numOfConnections() {
    return connections.size();
  }
}
