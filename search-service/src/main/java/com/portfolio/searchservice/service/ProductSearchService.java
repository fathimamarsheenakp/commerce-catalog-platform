package com.portfolio.searchservice.service;

import com.portfolio.searchservice.document.ProductDocument;
import com.portfolio.searchservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

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
}