package com.specsheetcentral.service;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import com.specsheetcentral.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    private Category createCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Microcontrollers");
        return category;
    }

    private Product createProduct() {
        Category category = createCategory();
        Product product = new Product();
        product.setId(1L);
        product.setName("Arduino Uno");
        product.setSku("ARD-UNO");
        product.setPrice(24.99);
        product.setStockQuantity(10);
        product.setCategory(category);
        product.setManufacturer("Arduino");
        return product;
    }

    @Test
    void findById_existingProduct_returnsResponse() {
        Product product = createProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertThat(result.getName()).isEqualTo("Arduino Uno");
        assertThat(result.getSku()).isEqualTo("ARD-UNO");
        assertThat(result.getPrice()).isEqualTo(24.99);
        assertThat(result.getCategoryName()).isEqualTo("Microcontrollers");
    }

    @Test
    void findById_nonExistingProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Product not found");
    }

    @Test
    void create_validRequest_returnsResponse() {
        Category category = createCategory();
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
            return p;
        });

        ProductResponse result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Arduino Uno");
        assertThat(result.getSku()).isEqualTo("ARD-UNO");
        assertThat(result.getManufacturer()).isEqualTo("Arduino");
        verify(productSpecRepository).saveAll(any());
    }

    @Test
    void create_nonexistentCategory_throwsException() {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Category not found");
    }

    @Test
    void delete_existingProduct_deletesSuccessfully() {
        Product product = createProduct();
        product.setDatasheetFilename("datasheet.pdf");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(1L);

        productService.delete(1L);

        verify(fileStorageService).deleteFile("datasheet.pdf");
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_nonexistentProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Product not found");
    }

    @Test
    void updateStock_validRequest_updatesQuantity() {
        Product product = createProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = productService.updateStock(1L, 25);

        assertThat(result.getStockQuantity()).isEqualTo(25);
    }

    @Test
    void findAll_withNoFilters_returnsAll() {
        Product product = createProduct();

        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));

        List<ProductResponse> results = productService.findAll(null, null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Arduino Uno");
    }
}