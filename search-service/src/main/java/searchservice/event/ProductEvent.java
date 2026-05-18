package searchservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private String eventType;

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private Boolean available;
    private Double rating;
}
