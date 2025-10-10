package io.beacon.userservice.user.entity;

import io.beacon.userservice.user.model.ConnectionType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;

@Node("User")
@Getter
@Setter
@RequiredArgsConstructor
public class User {

  @Id
  @GeneratedValue
  private UUID id;

  @Property("username")
  private final String username;

  @Property("fullName")
  private final String fullName;

  @Property("createdAt")
  @CreatedDate
  private Instant createdAt;

  @Relationship(type = ConnectionType.SENT_REQUEST, direction = Direction.OUTGOING)
  private Set<SentRequest> outgoingRequests;

  @Relationship(type = ConnectionType.SENT_REQUEST, direction = Direction.INCOMING)
  private Set<SentRequest> incomingRequests;
}
