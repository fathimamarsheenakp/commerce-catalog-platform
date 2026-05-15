package com.portfolio.searchservice.controller;

import com.portfolio.searchservice.document.ProductDocument;
import com.portfolio.searchservice.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Iterable<ProductDocument> getAllProducts() {
        return productSearchService.getAllProducts();
    }
}