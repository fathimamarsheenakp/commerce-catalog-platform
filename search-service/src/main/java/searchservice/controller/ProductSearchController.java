package searchservice.controller;

import searchservice.document.ProductDocument;
import searchservice.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

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
    public Iterable<ProductDocument> getAllProducts() {
        return productSearchService.getAllProducts();
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
}