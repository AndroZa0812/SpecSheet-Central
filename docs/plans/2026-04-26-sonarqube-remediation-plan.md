# SonarQube Assessment Remediation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Resolve all 4 SonarQube assessment findings: mark CSRF as safe, verify System.out removal, centralize CORS via env var, add core unit tests.

**Architecture:** CORS centralization in WebConfig.java driven by `CORS_ALLOWED_ORIGINS` env var. Per-controller `@CrossOrigin` annotations removed. Service-layer unit tests with Mockito. Controller tests with `@WebMvcTest`.

**Tech Stack:** Spring Boot 4.0, Java 21, JUnit 5, Mockito, Spring Security Test, H2

---

### Task 1: Mark CSRF Hotspot as SAFE in SonarQube

**Files:** None (SonarQube UI action)

**Step 1: Mark the CSRF hotspot**

Use the `sonarqube_change_security_hotspot_status` tool:
- hotspotKey: `AZ3GrjWDMMAbgOaviV-Z`
- status: `REVIEWED`
- resolution: `SAFE`
- comment: `Stateless JWT API — no cookie-based session auth. CSRF protection is not applicable.`

---

### Task 2: Verify System.out Removal (Already Done)

**Files:** None

**Step 1: Verify no System.out remains**

The grep confirmed zero `System.out` calls in the entire codebase. The DataSeeder already uses `log.info()` (SLF4J). This SonarQube issue (`AZ3GwPT_Ny07TWuQO8nU`) will resolve on next scan.

No code changes needed.

---

### Task 3: Centralize CORS Configuration

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/config/WebConfig.java`
- Modify: `backend/src/main/java/com/specsheetcentral/controller/AuthController.java` (remove @CrossOrigin)
- Modify: `backend/src/main/java/com/specsheetcentral/controller/CategoryController.java` (remove @CrossOrigin)
- Modify: `backend/src/main/java/com/specsheetcentral/controller/FileController.java` (remove @CrossOrigin)
- Modify: `backend/src/main/java/com/specsheetcentral/controller/OrderController.java` (remove @CrossOrigin)
- Modify: `backend/src/main/java/com/specsheetcentral/controller/ProductController.java` (remove @CrossOrigin)
- Modify: `backend/src/main/java/com/specsheetcentral/controller/ReviewController.java` (remove @CrossOrigin)

**Step 1: Update WebConfig.java with centralized CORS**

Replace `WebConfig.java` with:

```java
package com.specsheetcentral.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.isBlank()
                ? new String[]{}
                : allowedOrigins.split("\\s*,\\s*");
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);
    }
}
```

**Step 2: Remove @CrossOrigin from AuthController**

Remove `@CrossOrigin(origins = "*")` annotation and its import `import org.springframework.web.bind.annotation.CrossOrigin;` from `AuthController.java`.

**Step 3: Remove @CrossOrigin from CategoryController**

Remove `@CrossOrigin(origins = "*")` annotation and its import from `CategoryController.java`.

**Step 4: Remove @CrossOrigin from FileController**

Remove `@CrossOrigin(origins = "*")` annotation and its import from `FileController.java`.

**Step 5: Remove @CrossOrigin from OrderController**

Remove `@CrossOrigin(origins = "*")` annotation and its import from `OrderController.java`.

**Step 6: Remove @CrossOrigin from ProductController**

Remove `@CrossOrigin(origins = "*")` annotation and its import from `ProductController.java`.

**Step 7: Remove @CrossOrigin from ReviewController**

Remove `@CrossOrigin(origins = "*")` annotation and its import from `ReviewController.java`.

**Step 8: Mark CORS hotspots as FIXED in SonarQube**

Mark each of the 5 (actually 6 including ReviewController) CORS hotspots as REVIEWED → FIXED:
- `AZ3GrjVUMMAbgOaviV-L` (AuthController)
- `AZ3GrjVrMMAbgOaviV-O` (CategoryController)
- `AZ3GrjVbMMAbgOaviV-M` (FileController)
- `AZ3GrjVzMMAbgOaviV-P` (OrderController)
- `AZ3GrjVjMMAbgOaviV-N` (ProductController)

**Step 9: Compile and verify**

Run: `cd backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

---

### Task 4: Add CategoryService Unit Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/CategoryServiceTest.java`

**Step 1: Write CategoryServiceTest**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.model.Category;
import com.specsheetcentral.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");
    }

    @Test
    void findAll_returnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(electronics));

        List<Category> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void findById_existingId_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        Category result = categoryService.findById(1L);

        assertEquals("Electronics", result.getName());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.findById(99L));
    }

    @Test
    void create_savesAndReturnsCategory() {
        Category newCategory = new Category();
        newCategory.setName("Sensors");

        when(categoryRepository.save(newCategory)).thenReturn(newCategory);

        Category result = categoryService.create(newCategory);

        assertEquals("Sensors", result.getName());
        verify(categoryRepository).save(newCategory);
    }

    @Test
    void update_existingCategory_updatesName() {
        Category updatedData = new Category();
        updatedData.setName("Updated Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(categoryRepository.save(electronics)).thenReturn(electronics);

        Category result = categoryService.update(1L, updatedData);

        assertEquals("Updated Electronics", electronics.getName());
        verify(categoryRepository).save(electronics);
    }

    @Test
    void delete_callsRepositoryDelete() {
        categoryService.delete(1L);
        verify(categoryRepository).deleteById(1L);
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=CategoryServiceTest -pl . -q`
Expected: Tests pass

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/service/CategoryServiceTest.java
git commit -m "test: add CategoryService unit tests"
```

---

### Task 5: Add UserService Unit Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/UserServiceTest.java`

**Step 1: Write UserServiceTest**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encoded-password");
        testUser.setRole(User.Role.USER);
    }

    @Test
    void register_newEmail_createsUser() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.register("new@example.com", "password123", User.Role.USER);

        assertEquals("new@example.com", result.getEmail());
        assertEquals(User.Role.USER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("existing@example.com", "password", User.Role.USER));
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.findByEmail("test@example.com");

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void findByEmail_nonExistingUser_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.findByEmail("missing@example.com"));
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=UserServiceTest -pl . -q`
Expected: Tests pass

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/service/UserServiceTest.java
git commit -m "test: add UserService unit tests"
```

---

### Task 6: Add OrderService Unit Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/OrderServiceTest.java`

**Step 1: Write OrderServiceTest**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.model.*;
import com.specsheetcentral.repository.OrderRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setRole(User.Role.USER);

        testProduct = new Product();
        testProduct.setId(10L);
        testProduct.setName("Test Product");
        testProduct.setPrice(25.00);
        testProduct.setStockQuantity(100);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(50.00);
        testOrder.setStatus(Order.Status.PENDING);
    }

    @Test
    void findByUser_returnsOrdersForUser() {
        when(orderRepository.findByUserEmail("user@test.com")).thenReturn(List.of(testOrder));

        List<OrderResponse> result = orderService.findByUser("user@test.com");

        assertEquals(1, result.size());
        verify(orderRepository).findByUserEmail("user@test.com");
    }

    @Test
    void findAll_returnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(testOrder));

        List<OrderResponse> result = orderService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void create_insufficientStock_throwsException() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(200);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemReq));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalStateException.class,
                () -> orderService.create("user@test.com", request));
    }

    @Test
    void updateStatus_validStatus_updatesOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        OrderResponse result = orderService.updateStatus(1L, "IN_DELIVERY");

        assertEquals("IN_DELIVERY", result.getStatus());
    }

    @Test
    void create_nonexistentUser_throwsException() {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of());

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.create("missing@test.com", request));
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=OrderServiceTest -pl . -q`
Expected: Tests pass

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/service/OrderServiceTest.java
git commit -m "test: add OrderService unit tests"
```

---

### Task 7: Add ProductService Unit Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/ProductServiceTest.java`

**Step 1: Write ProductServiceTest**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductSpecRepository productSpecRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Microcontrollers");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Arduino Uno");
        testProduct.setSku("ARD-001");
        testProduct.setPrice(24.99);
        testProduct.setStockQuantity(50);
        testProduct.setCategory(testCategory);
        testProduct.setManufacturer("Arduino");
    }

    @Test
    void findById_existingProduct_returnsResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse result = productService.findById(1L);

        assertEquals("Arduino Uno", result.getName());
        assertEquals("Microcontrollers", result.getCategoryName());
    }

    @Test
    void findById_nonExistingProduct_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.findById(999L));
    }

    @Test
    void create_validRequest_returnsResponse() {
        ProductRequest request = new ProductRequest();
        request.setName("ESP32");
        request.setSku("ESP-001");
        request.setPrice(12.50);
        request.setStockQuantity(100);
        request.setCategoryId(1L);
        request.setManufacturer("Espressif");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductResponse result = productService.create(request);

        assertEquals("ESP32", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_nonexistentCategory_throwsException() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.create(request));
    }

    @Test
    void delete_existingProduct_deletesSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_nonexistentProduct_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.delete(999L));
    }

    @Test
    void updateStock_validRequest_updatesQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        ProductResponse result = productService.updateStock(1L, 200);

        assertEquals(200, result.getStockQuantity());
    }

    @Test
    void findAll_withNoFilters_returnsAll() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(testProduct));

        List<ProductResponse> results = productService.findAll(null, null, null, null, null);

        assertEquals(1, results.size());
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=ProductServiceTest -pl . -q`
Expected: Tests pass

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/service/ProductServiceTest.java
git commit -m "test: add ProductService unit tests"
```

---

### Task 8: Add ReviewService Unit Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/ReviewServiceTest.java`

**Step 1: Write ReviewServiceTest**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.ReviewRequest;
import com.specsheetcentral.dto.ReviewResponse;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.Review;
import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ReviewRepository;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductService productService;

    @InjectMocks private ReviewService reviewService;

    private Product testProduct;
    private User testUser;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);

        testUser = new User();
        testUser.setId(2L);
        testUser.setEmail("user@test.com");

        testReview = new Review();
        testReview.setId(10L);
        testReview.setProduct(testProduct);
        testReview.setUser(testUser);
        testReview.setRating(5);
        testReview.setComment("Great!");
    }

    @Test
    void createReview_validRequest_returnsResponse() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Great!");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByProductIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        ReviewResponse result = reviewService.createReview(1L, 2L, request);

        assertEquals(5, result.getRating());
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void createReview_duplicateReview_throwsException() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(4);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByProductIdAndUserId(1L, 2L)).thenReturn(Optional.of(testReview));

        assertThrows(IllegalStateException.class,
                () -> reviewService.createReview(1L, 2L, request));
    }

    @Test
    void createReview_nonexistentProduct_throwsException() {
        ReviewRequest request = new ReviewRequest();
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> reviewService.createReview(99L, 2L, request));
    }

    @Test
    void getReviewsByProduct_returnsList() {
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(testReview));

        List<ReviewResponse> result = reviewService.getReviewsByProduct(1L);

        assertEquals(1, result.size());
    }

    @Test
    void deleteReview_ownReview_succeeds() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(testReview));

        reviewService.deleteReview(10L, 2L, false);

        verify(reviewRepository).delete(testReview);
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void deleteReview_adminDeleteOtherUsersReview_succeeds() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(testReview));

        reviewService.deleteReview(10L, 99L, true);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    void deleteReview_nonOwner_throwsSecurityException() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(testReview));

        assertThrows(SecurityException.class,
                () -> reviewService.deleteReview(10L, 99L, false));
    }

    @Test
    void updateReview_byOwner_succeeds() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(3);
        request.setComment("Updated");

        when(reviewRepository.findById(10L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(testReview)).thenReturn(testReview);

        ReviewResponse result = reviewService.updateReview(10L, 2L, request);

        assertEquals(3, testReview.getRating());
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void updateReview_byNonOwner_throwsSecurityException() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(1);

        when(reviewRepository.findById(10L)).thenReturn(Optional.of(testReview));

        assertThrows(SecurityException.class,
                () -> reviewService.updateReview(10L, 99L, request));
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=ReviewServiceTest -pl . -q`
Expected: Tests pass

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/service/ReviewServiceTest.java
git commit -m "test: add ReviewService unit tests"
```

---

### Task 9: Add AuthController Integration Tests

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/controller/AuthControllerTest.java`

**Step 1: Write AuthControllerTest**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.AuthRequest;
import com.specsheetcentral.model.User;
import com.specsheetcentral.security.JwtFilter;
import com.specsheetcentral.security.JwtService;
import com.specsheetcentral.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.annotation.TestConstructor.Autowire;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(org.springframework.boot.test.context.TestConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void register_validInput_returnsToken() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("new@test.com");
        user.setRole(User.Role.USER);

        when(userService.register("new@test.com", "password123", User.Role.USER)).thenReturn(user);
        when(jwtService.generateToken("new@test.com", "USER")).thenReturn("jwt-token");

        AuthRequest request = new AuthRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void register_duplicateEmail_returnsBadRequest() throws Exception {
        when(userService.register("existing@test.com", "password", User.Role.USER))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        AuthRequest request = new AuthRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
```

**Step 2: Run tests**

Run: `cd backend && ./mvnw test -Dtest=AuthControllerTest -pl . -q`
Expected: Tests pass (may need adjustments for Spring Security test context)

**Step 3: Commit**

```bash
git add backend/src/test/java/com/specsheetcentral/controller/AuthControllerTest.java
git commit -m "test: add AuthController integration tests"
```

---

### Task 10: Run Full Test Suite and Verify Build

**Step 1: Run all tests**

Run: `cd backend && ./mvnw test -q`
Expected: All tests pass, BUILD SUCCESS

**Step 2: Verify CORS config works by checking compilation**

The WebConfig CORS config should compile and be picked up by Spring. No runtime test needed — the CORS integration is verified by Spring context loading.

**Step 3: Final commit (if any remaining unstaged files)**

```bash
git status
```

Review and commit anything remaining.

---

## Summary of SonarQube Hotspot Actions

After all code changes:

| Hotspot Key | Action |
|---|---|
| `AZ3GrjWDMMAbgOaviV-Z` | REVIEWED → SAFE (CSRF not needed for JWT) |
| `AZ3GrjVUMMAbgOaviV-L` | Will resolve on re-scan (CORS centralized) |
| `AZ3GrjVrMMAbgOaviV-O` | Will resolve on re-scan (CORS centralized) |
| `AZ3GrjVbMMAbgOaviV-M` | Will resolve on re-scan (CORS centralized) |
| `AZ3GrjVzMMAbgOaviV-P` | Will resolve on re-scan (CORS centralized) |
| `AZ3GrjVjMMAbgOaviV-N` | Will resolve on re-scan (CORS centralized) |