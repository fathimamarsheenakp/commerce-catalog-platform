package productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import productservice.event.ProductEvent;
import productservice.kafka.ProductEventProducer;
import productservice.model.Product;
import productservice.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public Product createProduct(Product product) {
        product.setId(UUID.randomUUID());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        productEventProducer.sendProductEvent(toEvent("PRODUCT_CREATED", savedProduct));

        return savedProduct;
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));
    }

    public Product updateProduct(UUID id, Product product) {
        Product existingProduct = getProductById(id);

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

        return updatedProduct;
    }

    public void deleteProduct(UUID id) {
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);
        productEventProducer.sendProductEvent(toEvent("PRODUCT_DELETED", existingProduct));
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