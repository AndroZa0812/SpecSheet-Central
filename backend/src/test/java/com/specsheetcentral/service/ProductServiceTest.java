package com.specsheetcentral.service;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductSpecRepository productSpecRepository;

    private ProductService productService;
    private Category category;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, productSpecRepository);
        category = new Category();
        category.setId(1L);
        category.setName("Microcontrollers");
    }

    @Test
    void findAllShouldReturnProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Arduino Uno");
        product.setSku("ARD-UNO");
        product.setPrice(24.99);
        product.setStockQuantity(10);
        product.setCategory(category);

        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));

        List<ProductResponse> results = productService.findAll(null, null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Arduino Uno");
        assertThat(results.get(0).getCategoryName()).isEqualTo("Microcontrollers");
    }

    @Test
    void findByIdShouldReturnProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Arduino Uno");
        product.setSku("ARD-UNO");
        product.setPrice(24.99);
        product.setStockQuantity(10);
        product.setCategory(category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertThat(result.getName()).isEqualTo("Arduino Uno");
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Product not found");
    }

    @Test
    void createShouldSaveProductWithSpecs() {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(1L);
        request.setManufacturer("Arduino");
        request.setSpecs(Map.of("Microcontroller", "ATmega328P"));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            p.setCategory(category);
            return p;
        });

        ProductResponse result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Arduino Uno");
        assertThat(result.getSku()).isEqualTo("ARD-UNO");
        verify(productSpecRepository).saveAll(any());
    }

    @Test
    void updateStockShouldModifyQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Arduino Uno");
        product.setSku("ARD-UNO");
        product.setPrice(24.99);
        product.setStockQuantity(10);
        product.setCategory(category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = productService.updateStock(1L, 25);

        assertThat(result.getStockQuantity()).isEqualTo(25);
    }

    @Test
    void deleteShouldRemoveProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void findAllShouldFilterBySearch() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<ProductResponse> results = productService.findAll("arduino", null, null, null, null);

        assertThat(results).isEmpty();
        verify(productRepository).findAll(any(Specification.class));
    }
}
