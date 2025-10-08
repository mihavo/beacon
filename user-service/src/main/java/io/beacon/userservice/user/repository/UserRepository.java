package io.beacon.userservice.user.repository;

import io.beacon.userservice.user.entity.User;
import java.util.UUID;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveNeo4jRepository<User, UUID> {
}
