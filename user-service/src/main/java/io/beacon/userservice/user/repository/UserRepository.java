package io.beacon.userservice.user.repository;

import io.beacon.userservice.user.entity.User;
import java.util.UUID;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveNeo4jRepository<User, UUID> {

  @Query("""
      MATCH (a: User {id : $userId}) - [r:FRIENDS_WITH] -  (b: User { id: $targetId } )
      RETURN COUNT(r) > 0
      """)
  boolean areFriends(UUID userId, UUID targetId);


  @Query("""
      MATCH (a : User {id: $userId} ), (b : User {id: $targetId} )
      OPTIONAL MATCH (a) - [r:SENT_REQUEST] - (b)
      RETURN COUNT(r) > 0
      """)
  boolean hasPendingRequest(UUID userId, UUID targetId);

  @Query("""
      MATCH ( a:User {id: $userId}), (b:User {id: $targetId} )
      WHERE NOT (a) - [:SENT_REQUEST] - (b) AND NOT (a) - [:FRIENDS_WITH] - (b)
      CREATE (a) - [:SENT_REQUEST {createdAt: datetime()}] -> (b)
      RETURN true
      """)
  boolean sendFriendRequest(UUID userId, UUID targetId);

  @Query("""
      MATCH (sender:User {id: $senderId}) -[r:SENT_REQUEST] -> (receiver:User {id: $receiverId})
      DELETE r
      CREATE (sender) - [:FRIENDS_WITH {since: datetime()}] -> (receiver)
      CREATE (receiver) - [:FRIENDS_WITH {since: datetime()}] -> (sender)
      RETURN true
      """)
  boolean acceptFriendRequest(UUID senderId, UUID receiverId);

  @Query("""
      MATCH (a:User {id: $userId}) - [r:SENT_REQUEST] -> (b:User {id: $targetId})
      DELETE r
      RETURN COUNT(r) > 0
      """)
  boolean deleteRequest(UUID userId, UUID targetId);

  @Query("""
      MATCH (a:User {id: $userId}) - [r:FRIENDS_WITH] - (b:User {id: $targetId})
      DELETE r
      RETURN COUNT(r) > 0
      """)
  boolean removeFriend(UUID userId, UUID targetId);
}
