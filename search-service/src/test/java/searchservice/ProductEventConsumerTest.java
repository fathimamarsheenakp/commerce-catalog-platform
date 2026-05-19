package searchservice;

import org.junit.jupiter.api.Test;
import searchservice.document.ProductDocument;
import searchservice.event.ProductEvent;
import searchservice.kafka.ProductEventConsumer;
import searchservice.repository.ProductSearchRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ProductEventConsumerTest {

    @Test
    void shouldSaveDocumentWhenProductCreated() {

        ProductSearchRepository repository =
                mock(ProductSearchRepository.class);

        ProductEventConsumer consumer =
                new ProductEventConsumer(repository);

        ProductEvent event = new ProductEvent(
                "PRODUCT_CREATED",
                UUID.randomUUID(),
                "MacBook Pro",
                "Apple laptop",
                "apple",
                "laptops",
                BigDecimal.valueOf(249999),
                true,
                4.9
        );

        consumer.consume(event);

        verify(repository, times(1))
                .save(any(ProductDocument.class));
    }

    @Test
    void shouldDeleteDocumentWhenProductDeleted() {

        ProductSearchRepository repository =
                mock(ProductSearchRepository.class);

        ProductEventConsumer consumer =
                new ProductEventConsumer(repository);

        UUID id = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                "PRODUCT_DELETED",
                id,
                "iPhone 16",
                "Apple smartphone",
                "apple",
                "mobiles",
                BigDecimal.valueOf(99999),
                true,
                4.8
        );

        consumer.consume(event);

        verify(repository, times(1))
                .deleteById(id);
    }
}