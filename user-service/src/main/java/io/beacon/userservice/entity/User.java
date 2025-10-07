package io.beacon.userservice.entity;

import io.beacon.userservice.model.RelationshipTypes;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("User")
@Getter
@Setter
@RequiredArgsConstructor
public class User {

  @Id
  @GeneratedValue(UUIDStringGenerator.class)
  private UUID id;

  @Property("username")
  private String username;

  @Property("fullName")
  private String fullName;

  @Property("createdAt")
  private final Instant createdAt = Instant.now();

  @Relationship(type = RelationshipTypes.SENT_REQUEST, direction = Direction.OUTGOING)
  private final Set<User> outgoingRequests = new HashSet<>();

  @Relationship(type = RelationshipTypes.SENT_REQUEST, direction = Direction.INCOMING)
  private final Set<User> incomingRequests = new HashSet<>();

  @Relationship(type = RelationshipTypes.FRIENDS_WITH, direction = Direction.OUTGOING)
  private final Set<User> friends = new HashSet<>();


}
