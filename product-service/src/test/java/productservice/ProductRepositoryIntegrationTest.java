package productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import productservice.model.Product;
import productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ProductRepositoryIntegrationTest {

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

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldFindProductById() {

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Dell XPS 15")
                .description("Dell laptop")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(189999))
                .available(true)
                .rating(4.7)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        Optional<Product> found =
                productRepository.findById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBrand()).isEqualTo("dell");
    }

    @Test
    void shouldDeleteProductById() {

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("iPhone 16")
                .description("Apple smartphone")
                .brand("apple")
                .category("mobiles")
                .price(BigDecimal.valueOf(99999))
                .available(true)
                .rating(4.8)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        productRepository.deleteById(product.getId());

        Optional<Product> found = productRepository.findById(product.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateProduct() {

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Lenovo ThinkPad")
                .description("Business laptop")
                .brand("lenovo")
                .category("laptops")
                .price(BigDecimal.valueOf(159999))
                .available(true)
                .rating(4.6)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        product.setPrice(BigDecimal.valueOf(149999));
        product.setRating(4.7);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        Optional<Product> updated =
                productRepository.findById(product.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(149999));
        assertThat(updated.get().getRating()).isEqualTo(4.7);
    }
}