package productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import productservice.event.ProductEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventProducer {

    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public void sendProductEvent(ProductEvent event) {
        log.info("Publishing product event to Kafka: type={}, id={}", event.getEventType(), event.getId());
        kafkaTemplate.send(TOPIC, event.getId().toString(), event);
    }
}