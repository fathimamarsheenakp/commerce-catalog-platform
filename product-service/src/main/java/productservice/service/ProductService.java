package productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import productservice.audit.AuditService;
import productservice.event.ProductEvent;
import productservice.exception.ProductNotFoundException;
import productservice.kafka.ProductEventProducer;
import productservice.model.Product;
import productservice.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;
    private final AuditService auditService;

    private void normalize(Product product) {
        if (product.getBrand() != null) {
            product.setBrand(product.getBrand().trim().toLowerCase());
        }

        if (product.getCategory() != null) {
            product.setCategory(product.getCategory().trim().toLowerCase());
        }
    }

    private String currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "system";
        }

        return authentication.getName();
    }

    public Product createProduct(Product product) {
        product.setId(UUID.randomUUID());

        normalize(product);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        productEventProducer.sendProductEvent(toEvent("PRODUCT_CREATED", savedProduct));

        auditService.log(
                currentUser(), "PRODUCT_CREATED", savedProduct.getId().toString());

        return savedProduct;
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found")
                );
    }

    public Product updateProduct(UUID id, Product product) {
        normalize(product);

        Product existingProduct = productRepository.findById(id).orElse(null);

        if (existingProduct == null) {
            product.setId(id);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            Product savedProduct = productRepository.save(product);
            productEventProducer.sendProductEvent(toEvent("PRODUCT_UPDATED", savedProduct));
            return savedProduct;
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setAvailable(product.getAvailable());
        existingProduct.setRating(product.getRating());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(existingProduct);
        productEventProducer.sendProductEvent(toEvent("PRODUCT_UPDATED", updatedProduct));

        auditService.log(
                currentUser(), "PRODUCT_UPDATED", updatedProduct.getId().toString());

        return updatedProduct;
    }

    public void deleteProduct(UUID id) {
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);
        productEventProducer.sendProductEvent(toEvent("PRODUCT_DELETED", existingProduct));

        auditService.log(
                currentUser(), "PRODUCT_DELETED", existingProduct.getId().toString());
    }

    private ProductEvent toEvent(String eventType, Product product) {
        return new ProductEvent(
                eventType,
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getCategory(),
                product.getPrice(),
                product.getAvailable(),
                product.getRating()
        );
    }
}