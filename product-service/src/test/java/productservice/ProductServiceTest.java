package productservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import productservice.event.ProductEvent;
import productservice.kafka.ProductEventProducer;
import productservice.model.Product;
import productservice.repository.ProductRepository;
import productservice.service.ProductService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventProducer productEventProducer;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {

        Product product = Product.builder()
                .name("MacBook Pro")
                .description("Apple laptop")
                .brand("apple")
                .category("laptops")
                .price(BigDecimal.valueOf(249999))
                .available(true)
                .rating(4.9)
                .build();

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product savedProduct =
                productService.createProduct(product);

        assertThat(savedProduct.getId()).isNotNull();

        verify(productRepository, times(1))
                .save(any(Product.class));

        verify(productEventProducer, times(1))
                .sendProductEvent(any(ProductEvent.class));
    }
    @Test
    void shouldGetProductById() {

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Dell XPS")
                .description("Dell laptop")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(189999))
                .available(true)
                .rating(4.7)
                .build();

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(product.getId());

        assertThat(result.getName()).isEqualTo("Dell XPS");

        verify(productRepository, times(1))
                .findById(product.getId());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        UUID id = UUID.randomUUID();

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.getProductById(id)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, times(1))
                .findById(id);
    }

    @Test
    void shouldUpdateProduct() {

        UUID id = UUID.randomUUID();

        Product existingProduct = Product.builder()
                .id(id)
                .name("Old Laptop")
                .description("Old description")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(100000))
                .available(true)
                .rating(4.0)
                .build();

        Product updatedData = Product.builder()
                .name("Dell XPS 15")
                .description("Updated Dell laptop")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(189999))
                .available(true)
                .rating(4.7)
                .build();

        when(productRepository.findById(id))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product updatedProduct =
                productService.updateProduct(id, updatedData);

        assertThat(updatedProduct.getName())
                .isEqualTo("Dell XPS 15");

        assertThat(updatedProduct.getPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(189999));

        verify(productRepository, times(1))
                .save(any(Product.class));

        verify(productEventProducer, times(1))
                .sendProductEvent(any(ProductEvent.class));
    }

    @Test
    void shouldDeleteProduct() {

        UUID id = UUID.randomUUID();

        Product existingProduct = Product.builder()
                .id(id)
                .name("iPhone 16")
                .description("Apple smartphone")
                .brand("apple")
                .category("mobiles")
                .price(BigDecimal.valueOf(99999))
                .available(true)
                .rating(4.8)
                .build();

        when(productRepository.findById(id))
                .thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(id);

        verify(productRepository, times(1))
                .delete(existingProduct);

        verify(productEventProducer, times(1))
                .sendProductEvent(any(ProductEvent.class));
    }

}