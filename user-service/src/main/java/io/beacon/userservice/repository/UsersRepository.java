package io.beacon.userservice.repository;

import io.beacon.userservice.entity.User;
import java.util.UUID;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends ReactiveNeo4jRepository<User, UUID> {
}
