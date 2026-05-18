package searchservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import searchservice.document.ProductDocument;
import searchservice.event.ProductEvent;
import searchservice.repository.ProductSearchRepository;

@Service
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchRepository productSearchRepository;

    @KafkaListener(topics = "product-events", groupId = "search-service-group")
    public void consume(ProductEvent event) {

        if ("PRODUCT_DELETED".equals(event.getEventType())) {
            productSearchRepository.deleteById(event.getId());
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
    }
}