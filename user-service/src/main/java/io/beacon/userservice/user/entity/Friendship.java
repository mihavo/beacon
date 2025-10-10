package io.beacon.userservice.user.entity;

import java.time.Instant;
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
public class Friendship {

  @Id
  @GeneratedValue
  private String id;


  private Instant since = Instant.now();


  @TargetNode
  private User friend;

}
