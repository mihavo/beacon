package io.beacon.userservice.config;

import io.beacon.userservice.grpc.clients.AuthGrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class UserTestsBase {

  private static Neo4j embeddedDatabaseServer;

  static synchronized Neo4j getInstance() {
    if (embeddedDatabaseServer == null) {
    embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
        .withDisabledServer()
        .build();
    }
    return embeddedDatabaseServer;
  }

  @DynamicPropertySource
  static void neo4jProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.neo4j.uri", getInstance()::boltURI);
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", () -> null);
  }

  @BeforeEach
  void cleanDatabase() {
    getInstance().defaultDatabaseService().executeTransactionally("MATCH (n) DETACH DELETE n");
  }

  @MockitoBean
  private AuthGrpcClient authGrpcClient;
}
