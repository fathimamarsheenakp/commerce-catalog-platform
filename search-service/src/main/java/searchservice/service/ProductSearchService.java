package searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import searchservice.document.ProductDocument;
import searchservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public ProductDocument save(ProductDocument productDocument) {
        return productSearchRepository.save(productDocument);
    }

    public Optional<ProductDocument> getById(UUID id) {
        return productSearchRepository.findById(id);
    }

    public Iterable<ProductDocument> getAllProducts() {
        return productSearchRepository.findAll();
    }

    public List<ProductDocument> search(String keyword) {
        return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category) {

        if (category != null && !category.isBlank()) {
            return productSearchRepository
                    .findByNameContainingOrDescriptionContainingAndCategory(keyword, keyword, category);
        }

        return productSearchRepository
                .findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category, String brand) {

        if (category != null && brand != null) {
            return productSearchRepository
                    .findByNameContainingOrDescriptionContainingAndCategoryAndBrand(keyword, keyword, category, brand);
        }

        if (category != null) {
            return productSearchRepository
                    .findByNameContainingOrDescriptionContainingAndCategory(keyword, keyword, category);
        }

        return productSearchRepository
                .findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category, String brand, String sort
    ) {

        List<ProductDocument> products;

        if (category != null && brand != null) {
            products = productSearchRepository
                    .findByNameContainingOrDescriptionContainingAndCategoryAndBrand(keyword, keyword, category, brand);
        }
        else if (category != null) {
            products = productSearchRepository
                    .findByNameContainingOrDescriptionContainingAndCategory(keyword, keyword, category);
        }
        else {
            products = productSearchRepository
                    .findByNameContainingOrDescriptionContaining(keyword, keyword);
        }

        List<ProductDocument> result = new ArrayList<>(products);

        if ("priceAsc".equals(sort)) {
            result.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        }

        if ("priceDesc".equals(sort)) {
            result.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
        }

        if ("rating".equals(sort)) {
            result.sort((a, b) -> b.getRating().compareTo(a.getRating()));
        }

        return result;
    }

    public Page<ProductDocument> searchWithPagination(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return productSearchRepository
                .findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    public Map<String, Long> getProductCountByCategory() throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .size(0)
                        .aggregations("categories", a -> a
                                .terms(t -> t
                                        .field("category.keyword")
                                )
                        ),
                ProductDocument.class
        );

        Map<String, Long> result = new HashMap<>();

        response.aggregations()
                .get("categories")
                .sterms()
                .buckets()
                .array()
                .forEach(bucket -> {
                    result.put(bucket.key().stringValue(), bucket.docCount());
                });

        return result;
    }
}