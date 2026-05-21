package searchservice.controller;

import org.springframework.data.domain.Page;
import searchservice.document.ProductDocument;
import searchservice.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/search/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @PostMapping
    public ProductDocument saveProduct(@RequestBody ProductDocument productDocument) {
        return productSearchService.save(productDocument);
    }

    @GetMapping("/{id}")
    public Optional<ProductDocument> getProduct(@PathVariable UUID id) {
        return productSearchService.getById(id);
    }

    @GetMapping
    public List<ProductDocument> getAllProducts() {
        List<ProductDocument> products = new ArrayList<>();
        productSearchService.getAllProducts().forEach(products::add);
        return products;
    }

//    @GetMapping("/search")
//    public List<ProductDocument> searchProducts(@RequestParam String keyword) {
//        return productSearchService.search(keyword);
//    }

//    @GetMapping("/search")
//    public List<ProductDocument> searchProducts(@RequestParam String keyword, @RequestParam(required = false) String category) {
//        return productSearchService.search(keyword, category);
//    }

//    @GetMapping("/search")
//    public List<ProductDocument> searchProducts(@RequestParam String keyword, @RequestParam(required = false) String category, @RequestParam(required = false) String brand) {
//        return productSearchService.search(keyword, category, brand);
//    }

    @GetMapping("/search")
    public List<ProductDocument> searchProducts(@RequestParam String keyword, @RequestParam(required = false) String category, @RequestParam(required = false) String brand, @RequestParam(required = false) String sort) {
        return productSearchService.search(keyword, category, brand, sort);
    }

    @GetMapping("/paged-search")
    public Page<ProductDocument> pagedSearch(@RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
        return productSearchService.searchWithPagination(keyword, page, size);
    }

    @GetMapping("/aggregations/categories")
    public Map<String, Long> getProductCountByCategory() throws IOException {
        return productSearchService.getProductCountByCategory();
    }

    @GetMapping("/aggregations/brands")
    public Map<String, Long> getProductCountByBrand() throws IOException {
        return productSearchService.getProductCountByBrand();
    }

    @GetMapping("/aggregations/brands/average-rating")
    public Map<String, Double> getAverageRatingByBrand() throws IOException {
        return productSearchService.getAverageRatingByBrand();
    }

    @GetMapping("/aggregations/categories/price-stats")
    public Map<String, Map<String, Double>> getPriceStatsByCategory() throws IOException {
        return productSearchService.getPriceStatsByCategory();
    }

    @GetMapping("/aggregations/stats")
    public Map<String, Object> getOverallProductStats() throws IOException {
        return productSearchService.getOverallProductStats();
    }
}