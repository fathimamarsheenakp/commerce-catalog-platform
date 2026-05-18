package searchservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import searchservice.document.ProductDocument;
import searchservice.event.ProductEvent;
import searchservice.repository.ProductSearchRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchRepository productSearchRepository;

    @KafkaListener(topics = "product-events", groupId = "search-service-group")
    public void consume(ProductEvent event) {
        log.info("Consumed product event from Kafka: type={}, id={}",
                event.getEventType(), event.getId());

        if ("PRODUCT_DELETED".equals(event.getEventType())) {
            productSearchRepository.deleteById(event.getId());
            log.info("Deleted product from Elasticsearch: id={}", event.getId());
            return;
        }

        ProductDocument document = ProductDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .brand(event.getBrand())
                .category(event.getCategory())
                .price(event.getPrice())
                .available(event.getAvailable())
                .rating(event.getRating())
                .build();

        productSearchRepository.save(document);
        log.info("Indexed product into Elasticsearch: id={}", event.getId());
    }
}