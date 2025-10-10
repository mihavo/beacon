package io.beacon.userservice.user.repository;

import io.beacon.userservice.connections.dto.UserInfo;
import io.beacon.userservice.user.entity.User;
import java.util.UUID;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveNeo4jRepository<User, UUID> {

  @Query("""
      MATCH (a: User {id : $userId}) - [r:FRIENDS_WITH] -  (b: User { id: $targetId } )
      RETURN COUNT(r) > 0;
      """)
  Mono<Boolean> areFriends(UUID userId, UUID targetId);

  @Query("""
          MATCH (a:User {id: $selfId}), (b:User {id: $targetId})
          RETURN CASE 
              WHEN EXISTS((a)-[:FRIENDS_WITH]-(b)) THEN 'FRIENDS_WITH'
              WHEN EXISTS((a)-[:SENT_REQUEST]->(b)) THEN 'SENT_REQUEST'
              WHEN EXISTS((b)-[:SENT_REQUEST]->(a)) THEN 'RECEIVED_REQUEST'
              ELSE 'NONE'
          END
      """)
  Mono<String> getRelationshipType(UUID selfId, UUID targetId);


  @Query("""
      MATCH (a : User {id: $userId} ), (b : User {id: $targetId} )
      OPTIONAL MATCH (a) - [r:SENT_REQUEST] -> (b)
      RETURN COUNT(r) > 0
      """)
  Mono<Boolean> hasPendingRequest(UUID userId, UUID targetId);

  @Query("""
      MATCH ( a:User {id: $userId}), (b:User {id: $targetId} )
      WHERE NOT (a) - [:SENT_REQUEST] - (b) AND NOT (a) - [:FRIENDS_WITH] - (b)
      CREATE (a) - [:SENT_REQUEST {createdAt: datetime()}] -> (b)
      RETURN true
      """)
  Mono<Boolean> sendFriendRequest(UUID userId, UUID targetId);

  @Query("""
      MATCH (sender:User {id: $senderId}) -[r:SENT_REQUEST] -> (receiver:User {id: $receiverId})
      DELETE r
      CREATE (sender) - [:FRIENDS_WITH {since: datetime()}] -> (receiver)
      CREATE (receiver) - [:FRIENDS_WITH {since: datetime()}] -> (sender)
      RETURN true
      """)
  Mono<Boolean> acceptFriendRequest(UUID senderId, UUID receiverId);

  @Query("""
      MATCH (a:User {id: $initiatorUserId}) - [r:SENT_REQUEST] -> (b:User {id: $selfUserId})
      DELETE r
      RETURN COUNT(r) > 0
      """)
  Mono<Boolean> deleteRequest(UUID selfUserId, UUID initiatorUserId);

  @Query("""
      MATCH (a:User {id: $userId}) - [r:FRIENDS_WITH] - (b:User {id: $targetId})
      DELETE r
      RETURN COUNT(r) > 0
      """)
  Mono<Boolean> removeFriend(UUID userId, UUID targetId);


  @Query("""
      MATCH (a:User {id: $userId})-[r:FRIENDS_WITH|SENT_REQUEST]-(b:User)
      RETURN
      b.fullName AS fullName,
       b.username as username,
             CASE 
               WHEN type(r) = 'FRIENDS_WITH' THEN 'FRIENDS_WITH'
               WHEN type(r) = 'SENT_REQUEST' AND startNode(r).id = $userId THEN 'SENT_REQUEST'
               WHEN type(r) = 'SENT_REQUEST' AND endNode(r).id = $userId THEN 'RECEIVED_REQUEST'
             END AS status,
             coalesce(r.since, r.createdAt) AS lastConnectionTimestamp
      ORDER BY lastConnectionTimestamp DESC
      """)
  Flux<UserInfo> getConnections(UUID userId);


}
