package searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import searchservice.document.ProductDocument;
import searchservice.dto.PagedProductsResponse;
import searchservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
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

    public PagedProductsResponse getProductsPage(
            int page,
            int size,
            String keyword,
            String category,
            String brand,
            String sort
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        String kw = normalizeKeyword(keyword);
        String cat = normalizeToken(category);
        String br = normalizeToken(brand);

        try {
            Query query = buildCatalogQuery(kw, cat, br);

            SearchResponse<ProductDocument> response = elasticsearchClient.search(
                    s -> {
                        s.index("products")
                                .from(safePage * safeSize)
                                .size(safeSize)
                                .query(query);
                        applyElasticsearchSort(s, sort);
                        return s;
                    },
                    ProductDocument.class
            );

            List<ProductDocument> content = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            long totalElements = response.hits().total() != null
                    ? response.hits().total().value()
                    : content.size();
            int totalPages = safeSize == 0
                    ? 0
                    : (int) Math.ceil((double) totalElements / safeSize);

            log.info(
                    "Catalog search: page={}, size={}, keyword={}, category={}, brand={}, sort={}, hits={}",
                    safePage,
                    safeSize,
                    kw,
                    cat,
                    br,
                    sort,
                    totalElements
            );

            return new PagedProductsResponse(content, safePage, safeSize, totalElements, totalPages);
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch catalog search failed", e);
        }
    }

    private static Query buildCatalogQuery(String keyword, String category, String brand) {
        if (keyword == null && category == null && brand == null) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (category != null) {
            bool.filter(f -> f.term(t -> t.field("category.keyword").value(category)));
        }
        if (brand != null) {
            bool.filter(f -> f.term(t -> t.field("brand.keyword").value(brand)));
        }
        if (keyword != null) {
            String pattern = "*" + keyword.toLowerCase() + "*";
            bool.must(m -> m.bool(inner -> inner
                    .should(s -> s.wildcard(w -> w.field("name").value(pattern)))
                    .should(s -> s.wildcard(w -> w.field("description").value(pattern)))
                    .should(s -> s.wildcard(w -> w.field("brand.keyword").value(pattern)))
                    .should(s -> s.wildcard(w -> w.field("category.keyword").value(pattern)))
                    .minimumShouldMatch("1")
            ));
        }

        return Query.of(q -> q.bool(bool.build()));
    }

    private static void applyElasticsearchSort(
            co.elastic.clients.elasticsearch.core.SearchRequest.Builder search,
            String sort
    ) {
        if ("priceAsc".equals(sort)) {
            search.sort(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
        } else if ("priceDesc".equals(sort)) {
            search.sort(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
        } else if ("rating".equals(sort)) {
            search.sort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)));
        } else {
            search.sort(s -> s.field(f -> f.field("name.keyword").order(SortOrder.Asc)));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Lowercase filter tokens to match product-service normalization. */
    private static String normalizeToken(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private static String normalizeKeyword(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private static Sort buildSort(String sort) {
        if ("priceAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("priceDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        if ("rating".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }
        return Sort.by(Sort.Direction.ASC, "name");
    }

    private static PagedProductsResponse toPagedResponse(Page<ProductDocument> page) {
        return new PagedProductsResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public List<ProductDocument> search(String keyword) {
        return productSearchRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category) {
        String cat = normalizeToken(category);

        if (cat != null) {
            return productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory(
                            keyword, keyword, cat);
        }

        return productSearchRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category, String brand) {
        String cat = normalizeToken(category);
        String br = normalizeToken(brand);

        if (cat != null && br != null) {
            return productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryAndBrand(
                            keyword, keyword, cat, br);
        }

        if (cat != null) {
            return productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory(
                            keyword, keyword, cat);
        }

        return productSearchRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<ProductDocument> search(String keyword, String category, String brand, String sort) {
        String cat = normalizeToken(category);
        String br = normalizeToken(brand);

        List<ProductDocument> products;

        if (cat != null && br != null) {
            products = productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryAndBrand(
                            keyword, keyword, cat, br);
        } else if (cat != null) {
            products = productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory(
                            keyword, keyword, cat);
        } else {
            products = productSearchRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
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
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
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