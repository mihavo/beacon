package io.beacon.userservice.user.entity;

import io.beacon.userservice.user.model.ConnectionType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Node("User")
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue
  private UUID id;

  @Property("username")
  private final String username;

  @Property("fullName")
  private final String fullName;

  @Property("password")
  private final String password;

  @Property("createdAt")
  @CreatedDate
  private Instant createdAt;

  @Relationship(type = ConnectionType.SENT_REQUEST, direction = Direction.OUTGOING)
  private Set<SentRequest> outgoingRequests;

  @Relationship(type = ConnectionType.SENT_REQUEST, direction = Direction.INCOMING)
  private Set<SentRequest> incomingRequests;

  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }
}
