package io.beacon.userservice.config;

import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;

@Configuration
public class Neo4jConfig {

  @Bean
  public org.neo4j.cypherdsl.core.renderer.Configuration cypherConfig() {
    return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig().withDialect(Dialect.NEO4J_5)
        .build();
  }

  @Bean
  public ReactiveNeo4jTransactionManager reactiveTransactionManager(Driver driver,
      ReactiveDatabaseSelectionProvider databaseNameProvider) {
    return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
  }

  @Bean
  public ApplicationRunner createFulltextIndex(Driver driver) {
    return args -> {
      try (Session session = driver.session()) {
        session.run("""
                CREATE FULLTEXT INDEX userSearch
                IF NOT EXISTS
                FOR (n:User)
                ON EACH [n.firstName, n.username]
            """);
      }
    };
  }
}
