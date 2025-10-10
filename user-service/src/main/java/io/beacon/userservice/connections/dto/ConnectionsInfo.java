package io.beacon.userservice.connections.dto;

import java.util.List;

public record ConnectionsInfo(List<UserInfo> connections) {

  public ConnectionsInfo {
    connections = connections == null ? List.of() : List.copyOf(connections);
  }

  public int numOfConnections() {
    return connections.size();
  }
}
