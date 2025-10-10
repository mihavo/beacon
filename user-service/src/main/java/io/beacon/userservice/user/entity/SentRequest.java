package io.beacon.userservice.user.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@Setter
@RequiredArgsConstructor
public class SentRequest {

  @Id
  @GeneratedValue
  public UUID id;


  private Instant createdAt;


  @TargetNode
  private User user;
}
