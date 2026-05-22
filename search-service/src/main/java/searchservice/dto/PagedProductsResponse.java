package searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import searchservice.document.ProductDocument;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedProductsResponse {

    private List<ProductDocument> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
