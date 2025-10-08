package io.beacon.userservice.entity;

import io.beacon.userservice.model.RelationshipTypes;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
@Builder
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue
  private UUID id;

  @Property("username")
  private String username;

  @Property("fullName")
  private String fullName;

  @Property("createdAt")
  @CreatedDate
  private Instant createdAt;

  @Relationship(type = RelationshipTypes.SENT_REQUEST, direction = Direction.OUTGOING)
  private Set<User> outgoingRequests;

  @Relationship(type = RelationshipTypes.SENT_REQUEST, direction = Direction.INCOMING)
  private Set<User> incomingRequests;

  @Relationship(type = RelationshipTypes.FRIENDS_WITH, direction = Direction.OUTGOING)
  private Set<User> friends;


}
