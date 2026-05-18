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

    public Map<String, Long> getProductCountByBrand() throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .size(0)
                        .aggregations("brands", a -> a
                                .terms(t -> t
                                        .field("brand.keyword")
                                )
                        ),
                ProductDocument.class
        );

        Map<String, Long> result = new HashMap<>();

        response.aggregations()
                .get("brands")
                .sterms()
                .buckets()
                .array()
                .forEach(bucket -> {
                    result.put(bucket.key().stringValue(), bucket.docCount());
                });

        return result;
    }

    public Map<String, Double> getAverageRatingByBrand() throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .size(0)
                        .aggregations("brands", a -> a
                                .terms(t -> t.field("brand.keyword"))
                                .aggregations("avg_rating", avg -> avg
                                        .avg(v -> v.field("rating"))
                                )
                        ),
                ProductDocument.class
        );

        Map<String, Double> result = new HashMap<>();

        response.aggregations()
                .get("brands")
                .sterms()
                .buckets()
                .array()
                .forEach(bucket -> {
                    double avgRating = bucket.aggregations()
                            .get("avg_rating")
                            .avg()
                            .value();
                    avgRating = Math.round(avgRating * 10.0) / 10.0;

                    result.put(bucket.key().stringValue(), avgRating);
                });

        return result;
    }

    public Map<String, Map<String, Double>> getPriceStatsByCategory() throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .size(0)
                        .aggregations("categories", a -> a
                                .terms(t -> t.field("category.keyword"))
                                .aggregations("min_price", min -> min.min(v -> v.field("price")))
                                .aggregations("max_price", max -> max.max(v -> v.field("price")))
                        ),
                ProductDocument.class
        );

        Map<String, Map<String, Double>> result = new HashMap<>();

        response.aggregations()
                .get("categories")
                .sterms()
                .buckets()
                .array()
                .forEach(bucket -> {
                    double minPrice = bucket.aggregations().get("min_price").min().value();
                    double maxPrice = bucket.aggregations().get("max_price").max().value();

                    result.put(bucket.key().stringValue(), Map.of(
                            "minPrice", minPrice,
                            "maxPrice", maxPrice
                    ));
                });

        return result;
    }

    public Map<String, Object> getOverallProductStats() throws IOException {

        SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> s
                        .index("products")
                        .size(0)
                        .aggregations("avg_rating", a -> a.avg(v -> v.field("rating")))
                        .aggregations("min_price", a -> a.min(v -> v.field("price")))
                        .aggregations("max_price", a -> a.max(v -> v.field("price"))),
                ProductDocument.class
        );

        double avgRating = response.aggregations().get("avg_rating").avg().value();
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        return Map.of(
                "totalProducts", response.hits().total().value(),
                "averageRating", avgRating,
                "minPrice", response.aggregations().get("min_price").min().value(),
                "maxPrice", response.aggregations().get("max_price").max().value()
        );
    }
}