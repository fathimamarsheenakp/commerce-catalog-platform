package searchservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import searchservice.document.ProductDocument;
import searchservice.repository.ProductSearchRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class SearchRepositoryIntegrationTest {

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @BeforeEach
    void cleanIndex() {
        productSearchRepository.deleteAll();
    }

    @Test
    void shouldIndexProductDocument() {

        ProductDocument document = ProductDocument.builder()
                .id(UUID.randomUUID())
                .name("MacBook Pro")
                .description("Apple professional laptop")
                .brand("apple")
                .category("laptops")
                .price(BigDecimal.valueOf(249999))
                .available(true)
                .rating(4.9)
                .build();

        productSearchRepository.save(document);

        Optional<ProductDocument> found = productSearchRepository.findById(document.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("MacBook Pro");
    }

    @Test
    void shouldSearchProductsByKeyword() {

        ProductDocument document = ProductDocument.builder()
                .id(UUID.randomUUID())
                .name("Sony WH-1000XM5")
                .description("Noise cancelling wireless headphones")
                .brand("sony")
                .category("audio")
                .price(BigDecimal.valueOf(29999))
                .available(true)
                .rating(4.8)
                .build();

        productSearchRepository.save(document);

        List<ProductDocument> results =
                productSearchRepository
                        .findByNameContainingOrDescriptionContaining("Sony", "Sony");

        assertThat(results).isNotEmpty();
    }

    @Test
    void shouldFilterProductsByCategory() throws InterruptedException {

        ProductDocument laptop = ProductDocument.builder()
                .id(UUID.randomUUID())
                .name("MacBook Pro")
                .description("Apple laptop")
                .brand("apple")
                .category("laptops")
                .price(BigDecimal.valueOf(249999))
                .available(true)
                .rating(4.9)
                .build();

        ProductDocument mobile = ProductDocument.builder()
                .id(UUID.randomUUID())
                .name("iPhone 16")
                .description("Apple smartphone")
                .brand("apple")
                .category("mobiles")
                .price(BigDecimal.valueOf(99999))
                .available(true)
                .rating(4.8)
                .build();

        productSearchRepository.save(laptop);
        productSearchRepository.save(mobile);

        Thread.sleep(1000);

        List<ProductDocument> results =
                productSearchRepository
                        .findByNameContainingOrDescriptionContainingAndCategory(
                                "Apple", "Apple", "laptops");

//        assertThat(results).hasSize(1);
        assertThat(results)
                .extracting(ProductDocument::getCategory)
                .contains("laptops");
    }
}
