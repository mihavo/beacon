package io.beacon.userservice;

import io.beacon.userservice.grpc.clients.AuthGrpcClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"user-friendship-events"})
class UserServiceApplicationTests {

  private static Neo4j embeddedDatabaseServer;

  @BeforeAll
  static void initializeNeo4j() {

    embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
        .withDisabledServer()
        .build();
  }

  @DynamicPropertySource
  static void neo4jProperties(DynamicPropertyRegistry registry) {

    registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", () -> null);
  }

  @AfterAll
  static void stopNeo4j() {
    if (embeddedDatabaseServer != null) {
      embeddedDatabaseServer.close();
    }
  }

  @MockitoBean
  private AuthGrpcClient authGrpcClient;

  @Test
    void contextLoads() {
    }

}
