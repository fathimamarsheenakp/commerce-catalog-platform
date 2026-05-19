package productservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class ProductServiceIntegrationTest {

    @Container
    static CassandraContainer<?> cassandra =
            new CassandraContainer<>("cassandra:4.1")
                    .withInitScript("init.cql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.cassandra.contact-points", cassandra::getHost);

        registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));

        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");

        registry.add("spring.cassandra.keyspace-name", () -> "product_catalog");

        registry.add("spring.cassandra.schema-action", () -> "create_if_not_exists");
    }

    @Test
    void contextLoads() {
    }
}