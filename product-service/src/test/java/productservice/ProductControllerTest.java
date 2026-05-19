package productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import productservice.controller.ProductController;
import productservice.exception.ProductNotFoundException;
import productservice.model.Product;
import productservice.security.JwtUtil;
import productservice.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetProductById() throws Exception {

        UUID id = UUID.randomUUID();

        Product product = Product.builder()
                .id(id)
                .name("MacBook Pro")
                .description("Apple laptop")
                .brand("apple")
                .category("laptops")
                .price(BigDecimal.valueOf(249999))
                .available(true)
                .rating(4.9)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productService.getProductById(id))
                .thenReturn(product);

        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("MacBook Pro"));
    }

    @Test
    void shouldCreateProduct() throws Exception {

        Product product = Product.builder()
                .name("iPhone 16")
                .description("Apple smartphone")
                .brand("apple")
                .category("mobiles")
                .price(BigDecimal.valueOf(99999))
                .available(true)
                .rating(4.8)
                .build();

        Product savedProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("iPhone 16")
                .description("Apple smartphone")
                .brand("apple")
                .category("mobiles")
                .price(BigDecimal.valueOf(99999))
                .available(true)
                .rating(4.8)
                .build();

        when(productService.createProduct(any(Product.class)))
                .thenReturn(savedProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 16"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        UUID id = UUID.randomUUID();

        Product updateRequest = Product.builder()
                .name("Dell XPS 15")
                .description("Updated Dell laptop")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(189999))
                .available(true)
                .rating(4.7)
                .build();

        Product updatedProduct = Product.builder()
                .id(id)
                .name("Dell XPS 15")
                .description("Updated Dell laptop")
                .brand("dell")
                .category("laptops")
                .price(BigDecimal.valueOf(189999))
                .available(true)
                .rating(4.7)
                .build();

        when(productService.updateProduct(eq(id), any(Product.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dell XPS 15"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/" + id))
                .andExpect(status().isNoContent());

        verify(productService, times(1))
                .deleteProduct(id);
    }

    @Test
    void shouldReturnBadRequestForInvalidProduct() throws Exception {

        Product product = Product.builder()
                .name("")
                .description("Test")
                .brand("Apple")
                .category("Mobiles")
                .price(BigDecimal.valueOf(-100))
                .available(true)
                .rating(4.5)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(productService.getProductById(id))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
}