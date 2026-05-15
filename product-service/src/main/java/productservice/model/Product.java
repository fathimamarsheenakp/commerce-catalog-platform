package productservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

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