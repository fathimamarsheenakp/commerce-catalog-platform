package productservice;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import productservice.event.ProductEvent;
import productservice.kafka.ProductEventProducer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ProductEventProducerTest {

    @Test
    void shouldSendProductEventToKafka() {

        KafkaTemplate<String, ProductEvent> kafkaTemplate =
                mock(KafkaTemplate.class);

        ProductEventProducer producer =
                new ProductEventProducer(kafkaTemplate);

        UUID id = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                "PRODUCT_CREATED",
                id,
                "MacBook Pro",
                "Apple laptop",
                "apple",
                "laptops",
                BigDecimal.valueOf(249999),
                true,
                4.9
        );

        producer.sendProductEvent(event);

        verify(kafkaTemplate, times(1))
                .send("product-events", id.toString(), event);
    }
}