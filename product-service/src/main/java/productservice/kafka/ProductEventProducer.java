package productservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import productservice.event.ProductEvent;

@Service
@RequiredArgsConstructor
public class ProductEventProducer {

    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public void sendProductEvent(ProductEvent event) {
        kafkaTemplate.send(TOPIC, event.getId().toString(), event);
    }
}