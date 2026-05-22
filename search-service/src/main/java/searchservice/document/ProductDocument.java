package searchservice.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(indexName = "products")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private UUID id;

    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private Boolean available;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}