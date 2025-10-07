package io.beacon.userservice.repository;

import io.beacon.userservice.entity.User;
import java.util.UUID;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;

public interface UsersRepository extends ReactiveNeo4jRepository<User, UUID> {

  UUID id(UUID id);
}
