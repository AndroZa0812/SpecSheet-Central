# SpecSheet Central Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a full-stack electronic components trading platform (SpecSheet Central) with a Svelte frontend, Spring Boot backend, PostgreSQL database, JWT authentication, product catalog with filtering/comparison, shopping cart, and admin inventory management.

**Architecture:** Separated Client-Server model. Frontend is a Svelte SPA consuming a REST API. Backend is Spring Boot with layered architecture (Controller → Service → Repository → JPA/PostgreSQL). File storage is local filesystem for product images and datasheets. JWT secures all admin endpoints and identifies users for orders.

**Tech Stack:** Svelte (Vite), Spring Boot 3 (Java 17, Maven), PostgreSQL 15, Spring Security + JWT, CSS, JPA/Hibernate

---

## Testing Requirement

Every task must include corresponding unit and/or integration tests before the commit step. Tests use JUnit 5 + Mockito for service-layer unit tests, `@WebMvcTest` for controller integration tests, `@DataJpaTest` for repository tests, and H2 in-memory database for persistence tests. Run `mvn test` to verify all tests pass before committing.

## Prerequisites

- Java 17+ installed
- Node.js 18+ installed
- PostgreSQL 15+ installed and running
- Maven installed

---

## Phase 1: Project Initialization & Database

### Task 1: Initialize Spring Boot Project

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/specsheetcentral/SpecSheetCentralApplication.java`
- Create: `backend/src/main/resources/application.properties`

**Step 1: Write `backend/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.specsheetcentral</groupId>
    <artifactId>specsheet-central</artifactId>
    <version>1.0.0</version>
    <name>SpecSheet Central</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 2: Write `backend/src/main/java/com/specsheetcentral/SpecSheetCentralApplication.java`**

```java
package com.specsheetcentral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpecSheetCentralApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpecSheetCentralApplication.class, args);
    }
}
```

**Step 3: Write `backend/src/main/resources/application.properties`**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/specsheet_central
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8080

file.upload-dir=uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.web.resources.static-locations=classpath:/static/,file:uploads/
```

**Step 4: Create PostgreSQL database**

Run: `psql -U postgres -c "CREATE DATABASE specsheet_central;"`
Expected: `CREATE DATABASE`

**Step 5: Verify Spring Boot starts**

Run: `cd backend && mvn spring-boot:run`
Expected: `Tomcat started on port(s): 8080` in logs, no stack traces

**Step 6: Commit**

```bash
git add backend/
git commit -m "chore: initialize Spring Boot project with PostgreSQL and JWT deps"
```

---

### Task 2: Create JPA Entities

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/model/Category.java`
- Create: `backend/src/main/java/com/specsheetcentral/model/Product.java`
- Create: `backend/src/main/java/com/specsheetcentral/model/ProductSpec.java`
- Create: `backend/src/main/java/com/specsheetcentral/model/User.java`
- Create: `backend/src/main/java/com/specsheetcentral/model/Order.java`
- Create: `backend/src/main/java/com/specsheetcentral/model/OrderItem.java`

**Step 1: Write `Category.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
```

**Step 2: Write `Product.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private String manufacturer;

    @Column
    private String imageUrl;

    @Column
    private String datasheetUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpec> specs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDatasheetUrl() { return datasheetUrl; }
    public void setDatasheetUrl(String datasheetUrl) { this.datasheetUrl = datasheetUrl; }
    public List<ProductSpec> getSpecs() { return specs; }
    public void setSpecs(List<ProductSpec> specs) { this.specs = specs; }
}
```

**Step 3: Write `ProductSpec.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product_specs")
public class ProductSpec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String specKey;

    @Column(nullable = false)
    private String specValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSpecKey() { return specKey; }
    public void setSpecKey(String specKey) { this.specKey = specKey; }
    public String getSpecValue() { return specValue; }
    public void setSpecValue(String specValue) { this.specValue = specValue; }
}
```

**Step 4: Write `User.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    public enum Role {
        USER, ADMIN
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}
```

**Step 5: Write `Order.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public enum Status {
        PENDING, IN_DELIVERY, DELIVERED, CANCELLED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
```

**Step 6: Write `OrderItem.java`**

```java
package com.specsheetcentral.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double priceAtPurchase;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(Double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
}
```

**Step 7: Verify schema generation**

Run: `cd backend && mvn spring-boot:run`
Expected: Application starts, Hibernate creates tables (check logs for `create table categories`, `create table products`, etc.)

**Step 8: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/model/
git commit -m "feat: add JPA entities for all domain models"
```

---

### Task 3: Initialize Svelte Frontend

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.svelte`
- Create: `frontend/src/app.css`

**Step 1: Write `frontend/package.json`**

```json
{
  "name": "specsheet-central-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "devDependencies": {
    "@sveltejs/vite-plugin-svelte": "^3.0.0",
    "svelte": "^4.2.0",
    "vite": "^5.0.0"
  },
  "dependencies": {
    "axios": "^1.6.0",
    "svelte-routing": "^2.11.0"
  }
}
```

**Step 2: Write `frontend/vite.config.js`**

```javascript
import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

export default defineConfig({
  plugins: [svelte()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080'
    }
  }
})
```

**Step 3: Write `frontend/index.html`**

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>SpecSheet Central</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```

**Step 4: Write `frontend/src/main.js`**

```javascript
import './app.css'
import App from './App.svelte'

const app = new App({
  target: document.getElementById('app'),
})

export default app
```

**Step 5: Write `frontend/src/App.svelte`**

```svelte
<script>
  import { Router, Route } from 'svelte-routing'
  import Home from './routes/Home.svelte'
</script>

<Router>
  <Route path="/" component={Home} />
</Router>
```

**Step 6: Write `frontend/src/app.css`**

```css
:root {
  --primary: #2563eb;
  --primary-dark: #1d4ed8;
  --danger: #dc2626;
  --warning: #f59e0b;
  --success: #16a34a;
  --gray-100: #f3f4f6;
  --gray-200: #e5e7eb;
  --gray-800: #1f2937;
  --gray-900: #111827;
}

* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #fff;
  color: var(--gray-900);
}
```

**Step 7: Install dependencies and verify**

Run:
```bash
cd frontend
npm install
npm run dev
```

Expected: `VITE v5.x.x  ready in xxx ms`, page loads at `http://localhost:5173`

**Step 8: Commit**

```bash
git add frontend/
git commit -m "chore: initialize Svelte frontend with Vite"
```

---

## Phase 2: Backend Core - Categories & Products

### Task 4: Category Repository, Service, Controller

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/repository/CategoryRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/service/CategoryService.java`
- Create: `backend/src/main/java/com/specsheetcentral/controller/CategoryController.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/CategoryRequest.java`

**Step 1: Write `CategoryRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
```

**Step 2: Write `CategoryService.java`**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.model.Category;
import com.specsheetcentral.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category category) {
        Category existing = findById(id);
        existing.setName(category.getName());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
```

**Step 3: Write `CategoryRequest.java`**

```java
package com.specsheetcentral.dto;

public class CategoryRequest {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Step 4: Write `CategoryController.java`**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.CategoryRequest;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public Category create(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return categoryService.create(category);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return categoryService.update(id, category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Step 5: Test the endpoint**

Run: `cd backend && mvn spring-boot:run`

In another terminal:
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Microcontrollers"}'
```
Expected: `{"id":1,"name":"Microcontrollers"}`

**Step 6: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/repository/CategoryRepository.java
backend/src/main/java/com/specsheetcentral/service/CategoryService.java
backend/src/main/java/com/specsheetcentral/controller/CategoryController.java
backend/src/main/java/com/specsheetcentral/dto/CategoryRequest.java
git commit -m "feat: add category CRUD endpoints"
```

---

### Task 5: Product Repository, Service, Controller

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/repository/ProductRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/repository/ProductSpecRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/ProductRequest.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/ProductResponse.java`
- Create: `backend/src/main/java/com/specsheetcentral/service/ProductService.java`
- Create: `backend/src/main/java/com/specsheetcentral/controller/ProductController.java`

**Step 1: Write `ProductRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
```

**Step 2: Write `ProductSpecRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {
    List<ProductSpec> findByProductId(Long productId);
}
```

**Step 3: Write `ProductRequest.java`**

```java
package com.specsheetcentral.dto;

import java.util.Map;

public class ProductRequest {
    private String name;
    private String sku;
    private Double price;
    private Integer stockQuantity;
    private Long categoryId;
    private String manufacturer;
    private String imageUrl;
    private String datasheetUrl;
    private Map<String, String> specs;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDatasheetUrl() { return datasheetUrl; }
    public void setDatasheetUrl(String datasheetUrl) { this.datasheetUrl = datasheetUrl; }
    public Map<String, String> getSpecs() { return specs; }
    public void setSpecs(Map<String, String> specs) { this.specs = specs; }
}
```

**Step 4: Write `ProductResponse.java`**

```java
package com.specsheetcentral.dto;

import java.util.Map;

public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private Double price;
    private Integer stockQuantity;
    private String categoryName;
    private String manufacturer;
    private String imageUrl;
    private String datasheetUrl;
    private Map<String, String> specs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDatasheetUrl() { return datasheetUrl; }
    public void setDatasheetUrl(String datasheetUrl) { this.datasheetUrl = datasheetUrl; }
    public Map<String, String> getSpecs() { return specs; }
    public void setSpecs(Map<String, String> specs) { this.specs = specs; }
}
```

**Step 5: Write `ProductService.java`**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.ProductSpec;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecRepository productSpecRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductSpecRepository productSpecRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSpecRepository = productSpecRepository;
    }

    public List<ProductResponse> findAll(String search, Long categoryId, String manufacturer, Double minPrice, Double maxPrice) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (manufacturer != null && !manufacturer.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("manufacturer")), manufacturer.toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setManufacturer(request.getManufacturer());
        product.setImageUrl(request.getImageUrl());
        product.setDatasheetUrl(request.getDatasheetUrl());

        Product saved = productRepository.save(product);

        if (request.getSpecs() != null) {
            List<ProductSpec> specs = request.getSpecs().entrySet().stream()
                .map(e -> {
                    ProductSpec spec = new ProductSpec();
                    spec.setProduct(saved);
                    spec.setSpecKey(e.getKey());
                    spec.setSpecValue(e.getValue());
                    return spec;
                })
                .collect(Collectors.toList());
            productSpecRepository.saveAll(specs);
            saved.setSpecs(specs);
        }

        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setManufacturer(request.getManufacturer());
        product.setImageUrl(request.getImageUrl());
        product.setDatasheetUrl(request.getDatasheetUrl());

        productSpecRepository.deleteAll(product.getSpecs());

        if (request.getSpecs() != null) {
            List<ProductSpec> specs = request.getSpecs().entrySet().stream()
                .map(e -> {
                    ProductSpec spec = new ProductSpec();
                    spec.setProduct(product);
                    spec.setSpecKey(e.getKey());
                    spec.setSpecValue(e.getValue());
                    return spec;
                })
                .collect(Collectors.toList());
            productSpecRepository.saveAll(specs);
            product.setSpecs(specs);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStockQuantity(quantity);
        return toResponse(productRepository.save(product));
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategoryName(product.getCategory().getName());
        response.setManufacturer(product.getManufacturer());
        response.setImageUrl(product.getImageUrl());
        response.setDatasheetUrl(product.getDatasheetUrl());
        if (product.getSpecs() != null) {
            Map<String, String> specs = product.getSpecs().stream()
                .collect(Collectors.toMap(ProductSpec::getSpecKey, ProductSpec::getSpecValue));
            response.setSpecs(specs);
        }
        return response;
    }
}
```

**Step 6: Write `ProductController.java`**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return productService.findAll(search, categoryId, manufacturer, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return productService.updateStock(id, quantity);
    }
}
```

**Step 7: Test product creation**

Run: `cd backend && mvn spring-boot:run`

First create a category:
```bash
curl -X POST http://localhost:8080/api/categories -H "Content-Type: application/json" -d '{"name":"Microcontrollers"}'
```

Then create a product:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Arduino Uno R3",
    "sku":"ARD-UNO-R3",
    "price":24.99,
    "stockQuantity":150,
    "categoryId":1,
    "manufacturer":"Arduino",
    "specs":{"Microcontroller":"ATmega328P","Operating Voltage":"5V"}
  }'
```
Expected: Product JSON with id 1, specs included

**Step 8: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/repository/ProductRepository.java
backend/src/main/java/com/specsheetcentral/repository/ProductSpecRepository.java
backend/src/main/java/com/specsheetcentral/dto/ProductRequest.java
backend/src/main/java/com/specsheetcentral/dto/ProductResponse.java
backend/src/main/java/com/specsheetcentral/service/ProductService.java
backend/src/main/java/com/specsheetcentral/controller/ProductController.java
git commit -m "feat: add product CRUD with filtering and stock patch"
```

---

### Task 6: File Upload for Images and Datasheets

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/service/FileStorageService.java`
- Create: `backend/src/main/java/com/specsheetcentral/controller/FileController.java`

**Step 1: Write `FileStorageService.java`**

```java
package com.specsheetcentral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
```

**Step 2: Write `FileController.java`**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
```

**Step 3: Test upload**

Run: `cd backend && mvn spring-boot:run`
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/test.png"
```
Expected: `{"url":"/uploads/xxxx.png"}`

**Step 4: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/service/FileStorageService.java
backend/src/main/java/com/specsheetcentral/controller/FileController.java
git commit -m "feat: add file upload for product images and datasheets"
```

---

## Phase 3: Authentication & Authorization

### Task 7: JWT Security Configuration

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/security/JwtService.java`
- Create: `backend/src/main/java/com/specsheetcentral/security/JwtFilter.java`
- Create: `backend/src/main/java/com/specsheetcentral/security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/com/specsheetcentral/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/AuthRequest.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/AuthResponse.java`

**Step 1: Write `JwtService.java`**

```java
package com.specsheetcentral.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET = "my-very-secret-key-that-is-at-least-32-bytes-long!";
    private static final long EXPIRATION = 86400000; // 24 hours

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getKey())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            return !parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Step 2: Write `CustomUserDetailsService.java`**

```java
package com.specsheetcentral.security;

import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

**Step 3: Write `JwtFilter.java`**

```java
package com.specsheetcentral.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isTokenValid(token)) {
                Claims claims = jwtService.parseToken(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

**Step 4: Write `SecurityConfig.java`**

```java
package com.specsheetcentral.config;

import com.specsheetcentral.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**Step 5: Write `AuthRequest.java`**

```java
package com.specsheetcentral.dto;

public class AuthRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

**Step 6: Write `AuthResponse.java`**

```java
package com.specsheetcentral.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String role;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
```

**Step 7: Write unit tests**

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/security/JwtServiceTest.java`

```java
package com.specsheetcentral.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtService.generateToken("admin@test.com", "ADMIN");
        assertThat(token).isNotNull();

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("admin@test.com");
        assertThat(claims.get("role")).isEqualTo("ADMIN");
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtService.generateToken("user@test.com", "USER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid-token")).isFalse();
    }
}
```

**Step 8: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/security/
git add backend/src/main/java/com/specsheetcentral/config/
git add backend/src/main/java/com/specsheetcentral/dto/AuthRequest.java
git add backend/src/main/java/com/specsheetcentral/dto/AuthResponse.java
git commit -m "feat: configure JWT security and role-based access control"
```

---

### Task 8: Auth Controller and User Service

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/repository/UserRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/service/UserService.java`
- Create: `backend/src/main/java/com/specsheetcentral/controller/AuthController.java`

**Step 1: Write `UserRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

**Step 2: Write `UserService.java`**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String password, User.Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
```

**Step 3: Write `AuthController.java`**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.AuthRequest;
import com.specsheetcentral.dto.AuthResponse;
import com.specsheetcentral.model.User;
import com.specsheetcentral.security.JwtService;
import com.specsheetcentral.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        User user = userService.register(request.getEmail(), request.getPassword(), User.Role.USER);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userService.findByEmail(auth.getName());
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }
}
```

**Step 4: Test registration and login**

Run: `cd backend && mvn spring-boot:run`

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```
Expected: `{"token":"...","email":"user@example.com","role":"USER"}`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```
Expected: Same token response

**Step 5: Write unit tests**

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/UserServiceTest.java`
- Create: `backend/src/test/java/com/specsheetcentral/controller/AuthControllerTest.java`

Content for `UserServiceTest.java`:

```java
package com.specsheetcentral.service;

import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.register("new@test.com", "password", User.Role.USER);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        User existing = new User();
        existing.setEmail("existing@test.com");
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.register("existing@test.com", "password", User.Role.USER))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Email already registered");
    }

    @Test
    void shouldFindByEmail() {
        User user = new User();
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@test.com");

        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }
}
```

Content for `AuthControllerTest.java`:

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.AuthRequest;
import com.specsheetcentral.dto.AuthResponse;
import com.specsheetcentral.model.User;
import com.specsheetcentral.security.JwtService;
import com.specsheetcentral.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    void registerShouldReturnToken() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");
        user.setRole(User.Role.USER);

        when(userService.register("user@test.com", "password", User.Role.USER)).thenReturn(user);
        when(jwtService.generateToken("user@test.com", "USER")).thenReturn("test-jwt-token");

        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "password");
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        User user = new User();
        user.setEmail("user@test.com");
        user.setRole(User.Role.USER);
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(jwtService.generateToken("user@test.com", "USER")).thenReturn("test-jwt-token");

        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.email").value("user@test.com"));
    }
}
```

**Step 6: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/repository/UserRepository.java
backend/src/main/java/com/specsheetcentral/service/UserService.java
backend/src/main/java/com/specsheetcentral/controller/AuthController.java
git commit -m "feat: add user registration, login, and JWT issuance"
```

---

## Phase 4: Backend - Orders

### Task 9: Order Service and Controller

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/repository/OrderRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/repository/OrderItemRepository.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/OrderRequest.java`
- Create: `backend/src/main/java/com/specsheetcentral/dto/OrderResponse.java`
- Create: `backend/src/main/java/com/specsheetcentral/service/OrderService.java`
- Create: `backend/src/main/java/com/specsheetcentral/controller/OrderController.java`

**Step 1: Write `OrderRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserEmail(String email);
}
```

**Step 2: Write `OrderItemRepository.java`**

```java
package com.specsheetcentral.repository;

import com.specsheetcentral.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
```

**Step 3: Write `OrderRequest.java`**

```java
package com.specsheetcentral.dto;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> items;

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
```

**Step 4: Write `OrderResponse.java`**

```java
package com.specsheetcentral.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private String userEmail;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<OrderItemResponse> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double priceAtPurchase;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPriceAtPurchase() { return priceAtPurchase; }
        public void setPriceAtPurchase(Double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
    }
}
```

**Step 5: Write `OrderService.java`**

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.model.Order;
import com.specsheetcentral.model.OrderItem;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.OrderRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse create(String userEmail, OrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);

        double total = 0;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtPurchase(product.getPrice());
            order.getItems().add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public List<OrderResponse> findByUser(String userEmail) {
        return orderRepository.findByUserEmail(userEmail).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Order.Status.valueOf(status));
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setItems(order.getItems().stream().map(item -> {
            OrderResponse.OrderItemResponse i = new OrderResponse.OrderItemResponse();
            i.setProductId(item.getProduct().getId());
            i.setProductName(item.getProduct().getName());
            i.setQuantity(item.getQuantity());
            i.setPriceAtPurchase(item.getPriceAtPurchase());
            return i;
        }).collect(Collectors.toList()));
        return response;
    }
}
```

**Step 6: Write `OrderController.java`**

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@AuthenticationPrincipal UserDetails user,
                                @RequestBody OrderRequest request) {
        return orderService.create(user.getUsername(), request);
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(@AuthenticationPrincipal UserDetails user) {
        return orderService.findByUser(user.getUsername());
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.findAll();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }
}
```

**Step 7: Test order creation**

Run: `cd backend && mvn spring-boot:run`

Register and get token:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"buyer@example.com","password":"password"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

Create order:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
```
Expected: Order JSON with totalAmount, status PENDING, stock reduced

**Step 8: Write unit tests**

**Files:**
- Create: `backend/src/test/java/com/specsheetcentral/service/OrderServiceTest.java`
- Create: `backend/src/test/java/com/specsheetcentral/controller/OrderControllerTest.java`

Content for `OrderServiceTest.java`:

```java
package com.specsheetcentral.service;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.model.Order;
import com.specsheetcentral.model.OrderItem;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.OrderRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private OrderService orderService;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, userRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("buyer@test.com");

        product = new Product();
        product.setId(1L);
        product.setName("Arduino Uno");
        product.setPrice(24.99);
        product.setStockQuantity(10);
    }

    @Test
    void shouldCreateOrderAndReduceStock() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        OrderResponse response = orderService.create("buyer@test.com", request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualTo(49.98);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldThrowOnInsufficientStock() {
        product.setStockQuantity(1);
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(5);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.create("buyer@test.com", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldFindOrdersByUser() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(49.98);
        order.setStatus(Order.Status.PENDING);
        order.setItems(List.of());

        when(orderRepository.findByUserEmail("buyer@test.com")).thenReturn(List.of(order));

        List<OrderResponse> results = orderService.findByUser("buyer@test.com");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserEmail()).isEqualTo("buyer@test.com");
    }

    @Test
    void shouldUpdateOrderStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateStatus(1L, "DELIVERED");

        assertThat(response.getStatus()).isEqualTo("DELIVERED");
    }
}
```

Content for `OrderControllerTest.java`:

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "buyer@test.com")
    void createShouldReturnOrder() throws Exception {
        OrderResponse resp = new OrderResponse();
        resp.setId(1L);
        resp.setUserEmail("buyer@test.com");
        resp.setTotalAmount(49.98);
        resp.setStatus("PENDING");

        when(orderService.create(eq("buyer@test.com"), any(OrderRequest.class))).thenReturn(resp);

        String body = "{\"items\":[{\"productId\":1,\"quantity\":2}]}";

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com")
    void getMyOrdersShouldReturnList() throws Exception {
        OrderResponse resp = new OrderResponse();
        resp.setId(1L);
        resp.setUserEmail("buyer@test.com");

        when(orderService.findByUser("buyer@test.com")).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/orders/my"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userEmail").value("buyer@test.com"));
    }
}
```

**Step 9: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/repository/OrderRepository.java
backend/src/main/java/com/specsheetcentral/repository/OrderItemRepository.java
backend/src/main/java/com/specsheetcentral/dto/OrderRequest.java
backend/src/main/java/com/specsheetcentral/dto/OrderResponse.java
backend/src/main/java/com/specsheetcentral/service/OrderService.java
backend/src/main/java/com/specsheetcentral/controller/OrderController.java
git commit -m "feat: add order creation, history, and status management"
```

---

## Phase 5: Frontend - Core UI & Navigation

### Task 10: API Client and Stores

**Files:**
- Create: `frontend/src/lib/api.js`
- Create: `frontend/src/lib/stores.js`
- Create: `frontend/src/lib/utils.js`

**Step 1: Write `frontend/src/lib/api.js`**

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

export default api
```

**Step 2: Write `frontend/src/lib/stores.js`**

```javascript
import { writable } from 'svelte/store'

function createAuthStore() {
  const stored = localStorage.getItem('user')
  const { subscribe, set } = writable(stored ? JSON.parse(stored) : null)

  return {
    subscribe,
    login: (user, token) => {
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      set(user)
    },
    logout: () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      set(null)
    }
  }
}

export const auth = createAuthStore()

function createCartStore() {
  const stored = localStorage.getItem('cart')
  const { subscribe, set, update } = writable(stored ? JSON.parse(stored) : [])

  return {
    subscribe,
    add: (product) => update(items => {
      const existing = items.find(i => i.id === product.id)
      if (existing) {
        existing.quantity += 1
      } else {
        items.push({ ...product, quantity: 1 })
      }
      localStorage.setItem('cart', JSON.stringify(items))
      return [...items]
    }),
    remove: (productId) => update(items => {
      const filtered = items.filter(i => i.id !== productId)
      localStorage.setItem('cart', JSON.stringify(filtered))
      return filtered
    }),
    updateQuantity: (productId, quantity) => update(items => {
      const item = items.find(i => i.id === productId)
      if (item) {
        item.quantity = quantity
        if (quantity <= 0) {
          return items.filter(i => i.id !== productId)
        }
      }
      localStorage.setItem('cart', JSON.stringify(items))
      return [...items]
    }),
    clear: () => {
      localStorage.removeItem('cart')
      set([])
    }
  }
}

export const cart = createCartStore()
```

**Step 3: Write `frontend/src/lib/utils.js`**

```javascript
export function formatPrice(price) {
  return `$${price.toFixed(2)}`
}

export function getStockStatus(quantity) {
  if (quantity === 0) return 'Out of Stock'
  if (quantity < 10) return 'Low Stock'
  return 'In Stock'
}

export function getStockClass(quantity) {
  if (quantity === 0) return 'out'
  if (quantity < 10) return 'low'
  return 'in'
}
```

**Step 4: Commit**

```bash
git add frontend/src/lib/api.js
frontend/src/lib/stores.js
frontend/src/lib/utils.js
git commit -m "feat: add API client, auth store, and cart store"
```

---

### Task 11: Navbar and Routing

**Files:**
- Create: `frontend/src/components/Navbar.svelte`
- Modify: `frontend/src/App.svelte`
- Create: `frontend/src/routes/Login.svelte`
- Create: `frontend/src/routes/Register.svelte`
- Create placeholder routes: Catalog, ProductDetail, Compare, Cart, Admin

**Step 1: Write `Navbar.svelte`**

```svelte
<script>
  import { Link } from 'svelte-routing'
  import { auth, cart } from '../lib/stores.js'
  import { navigate } from 'svelte-routing'

  let cartCount = 0
  cart.subscribe(items => {
    cartCount = items.reduce((sum, i) => sum + i.quantity, 0)
  })

  function handleLogout() {
    auth.logout()
    navigate('/')
  }

  let searchQuery = ''
  function handleSearch(e) {
    if (e.key === 'Enter' && searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`)
    }
  }
</script>

<nav class="navbar">
  <div class="nav-brand">
    <Link to="/">
      <span class="logo">S</span>
      <span class="brand-text">SpecSheet Central</span>
    </Link>
  </div>

  <div class="nav-links">
    <Link to="/">Home</Link>
    <Link to="/products">Products</Link>
    <Link to="/compare">Compare</Link>
    {#if $auth?.role === 'ADMIN'}
      <Link to="/admin">Admin</Link>
    {/if}
  </div>

  <div class="nav-search">
    <input
      type="text"
      placeholder="Search for components..."
      bind:value={searchQuery}
      on:keypress={handleSearch}
    />
  </div>

  <div class="nav-actions">
    <Link to="/cart" class="cart-link">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="9" cy="21" r="1"></circle>
        <circle cx="20" cy="21" r="1"></circle>
        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
      </svg>
      {#if cartCount > 0}
        <span class="cart-badge">{cartCount}</span>
      {/if}
    </Link>

    {#if $auth}
      <span class="user-email">{$auth.email}</span>
      <button class="btn btn-ghost" on:click={handleLogout}>Logout</button>
    {:else}
      <Link to="/login">Login</Link>
    {/if}
  </div>
</nav>

<style>
  .navbar {
    display: flex;
    align-items: center;
    gap: 2rem;
    padding: 0.75rem 2rem;
    border-bottom: 1px solid var(--gray-200);
    background: white;
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .nav-brand :global(a) {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    color: var(--gray-900);
    font-weight: 700;
    font-size: 1.25rem;
  }

  .logo {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background: var(--primary);
    color: white;
    border-radius: 8px;
    font-weight: 700;
  }

  .nav-links {
    display: flex;
    gap: 1.5rem;
  }

  .nav-links :global(a) {
    text-decoration: none;
    color: var(--gray-800);
    font-weight: 500;
  }

  .nav-links :global(a:hover) {
    color: var(--primary);
  }

  .nav-search {
    flex: 1;
    max-width: 400px;
  }

  .nav-search input {
    width: 100%;
    padding: 0.5rem 1rem;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
    background: var(--gray-100);
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .nav-actions :global(a) {
    text-decoration: none;
    color: var(--gray-800);
  }

  .cart-link {
    position: relative;
    display: flex;
    align-items: center;
  }

  .cart-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background: var(--danger);
    color: white;
    font-size: 0.75rem;
    padding: 0 6px;
    border-radius: 10px;
    min-width: 18px;
    text-align: center;
  }

  .user-email {
    font-size: 0.875rem;
    color: var(--gray-800);
  }

  .btn {
    padding: 0.5rem 1rem;
    border-radius: 6px;
    border: none;
    cursor: pointer;
    font-size: 0.875rem;
  }

  .btn-ghost {
    background: transparent;
    color: var(--gray-800);
  }

  .btn-ghost:hover {
    background: var(--gray-100);
  }
</style>
```

**Step 2: Write `Login.svelte`**

```svelte
<script>
  import { navigate } from 'svelte-routing'
  import api from '../lib/api.js'
  import { auth } from '../lib/stores.js'

  let email = ''
  let password = ''
  let error = ''

  async function handleSubmit() {
    try {
      const res = await api.post('/auth/login', { email, password })
      auth.login({ email: res.data.email, role: res.data.role }, res.data.token)
      navigate('/')
    } catch (e) {
      error = e.response?.data?.message || 'Login failed'
    }
  }
</script>

<div class="auth-page">
  <div class="auth-card">
    <h1>Login</h1>
    {#if error}
      <div class="error">{error}</div>
    {/if}
    <form on:submit|preventDefault={handleSubmit}>
      <label>
        Email
        <input type="email" bind:value={email} required />
      </label>
      <label>
        Password
        <input type="password" bind:value={password} required />
      </label>
      <button type="submit" class="btn btn-primary">Login</button>
    </form>
    <p>Don't have an account? <a href="/register">Register</a></p>
  </div>
</div>

<style>
  .auth-page {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    background: var(--gray-100);
  }

  .auth-card {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    width: 100%;
    max-width: 400px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  }

  h1 { margin-bottom: 1.5rem; font-size: 1.5rem; }

  label {
    display: block;
    margin-bottom: 1rem;
    font-size: 0.875rem;
    font-weight: 500;
  }

  input {
    display: block;
    width: 100%;
    margin-top: 0.25rem;
    padding: 0.5rem;
    border: 1px solid var(--gray-200);
    border-radius: 6px;
  }

  .btn-primary {
    width: 100%;
    background: var(--primary);
    color: white;
    padding: 0.75rem;
    border: none;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
  }

  .error {
    color: var(--danger);
    margin-bottom: 1rem;
    font-size: 0.875rem;
  }
</style>
```

**Step 3: Write `Register.svelte`** (same structure as Login, call `/auth/register`)

Copy Login.svelte and change:
- Title: "Register"
- API endpoint: `/auth/register`
- Link text: "Already have an account? Login"
- href: `/login`

**Step 4: Modify `App.svelte`**

```svelte
<script>
  import { Router, Route } from 'svelte-routing'
  import Navbar from './components/Navbar.svelte'
  import Home from './routes/Home.svelte'
  import Catalog from './routes/Catalog.svelte'
  import ProductDetail from './routes/ProductDetail.svelte'
  import Compare from './routes/Compare.svelte'
  import Cart from './routes/Cart.svelte'
  import Admin from './routes/Admin.svelte'
  import Login from './routes/Login.svelte'
  import Register from './routes/Register.svelte'
</script>

<Router>
  <Navbar />
  <main>
    <Route path="/" component={Home} />
    <Route path="/products" component={Catalog} />
    <Route path="/products/:id" component={ProductDetail} />
    <Route path="/compare" component={Compare} />
    <Route path="/cart" component={Cart} />
    <Route path="/admin" component={Admin} />
    <Route path="/login" component={Login} />
    <Route path="/register" component={Register} />
  </main>
</Router>
```

**Step 5: Create placeholder route files**

Create minimal files so it compiles:
- `frontend/src/routes/Catalog.svelte`
- `frontend/src/routes/ProductDetail.svelte`
- `frontend/src/routes/Compare.svelte`
- `frontend/src/routes/Cart.svelte`
- `frontend/src/routes/Admin.svelte`

Each with:
```svelte
<script>
</script>

<div>Page</div>
```

**Step 6: Verify dev server**

Run: `cd frontend && npm run dev`
Expected: No compile errors, navbar visible at localhost:5173

**Step 7: Commit**

```bash
git add frontend/src/components/Navbar.svelte
frontend/src/App.svelte
frontend/src/routes/Login.svelte
frontend/src/routes/Register.svelte
frontend/src/routes/Catalog.svelte
frontend/src/routes/ProductDetail.svelte
frontend/src/routes/Compare.svelte
frontend/src/routes/Cart.svelte
frontend/src/routes/Admin.svelte
git commit -m "feat: add navbar, routing, login and register pages"
```

---

## Phase 6: Frontend - Home & Catalog

### Task 12: Home Page

**Files:**
- Create: `frontend/src/routes/Home.svelte`

**Step 1: Write `Home.svelte`**

```svelte
<script>
  import { onMount } from 'svelte'
  import { Link } from 'svelte-routing'
  import api from '../lib/api.js'

  let categories = []

  onMount(async () => {
    const res = await api.get('/categories')
    categories = res.data
  })
</script>

<div class="home">
  <section class="hero">
    <div class="hero-content">
      <span class="badge">New Arrivals Weekly</span>
      <h1>Premium Electronic Components for Your Projects</h1>
      <p>Browse thousands of quality components from Arduino boards to sensors, LEDs, and more. Fast shipping and expert support.</p>
      <div class="hero-buttons">
        <Link to="/products" class="btn btn-primary">Shop Now</Link>
        <Link to="/products" class="btn btn-secondary">Learn More</Link>
      </div>
    </div>
  </section>

  <section class="categories">
    <h2>Shop by Category</h2>
    <div class="category-grid">
      {#each categories as category}
        <Link to={`/products?categoryId=${category.id}`} class="category-card">
          <div class="category-icon">
            {#if category.name === 'Microcontrollers'}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 9h6v6H9z"/></svg>
            {:else if category.name === 'Sensors'}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
            {:else if category.name === 'LEDs'}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><path d="M9 18h6M10 22h4M12 2v1M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"/></svg>
            {:else}
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#dc2626" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            {/if}
          </div>
          <span class="category-name">{category.name}</span>
        </Link>
      {/each}
    </div>
  </section>
</div>

<style>
  .hero {
    background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
    color: white;
    padding: 4rem 2rem;
  }

  .hero-content {
    max-width: 1200px;
    margin: 0 auto;
  }

  .badge {
    display: inline-block;
    background: rgba(255,255,255,0.2);
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    margin-bottom: 1rem;
  }

  h1 {
    font-size: 3rem;
    font-weight: 700;
    max-width: 600px;
    line-height: 1.2;
    margin-bottom: 1rem;
  }

  p {
    font-size: 1.125rem;
    max-width: 500px;
    opacity: 0.9;
    margin-bottom: 2rem;
  }

  .hero-buttons {
    display: flex;
    gap: 1rem;
  }

  .hero-buttons :global(.btn) {
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    text-decoration: none;
    font-weight: 500;
  }

  .hero-buttons :global(.btn-primary) {
    background: white;
    color: var(--primary);
  }

  .hero-buttons :global(.btn-secondary) {
    background: transparent;
    color: white;
    border: 1px solid white;
  }

  .categories {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem;
  }

  .categories h2 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }

  .category-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .category-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
    padding: 1.5rem;
    border: 1px solid var(--gray-200);
    border-radius: 12px;
    text-decoration: none;
    color: var(--gray-900);
    transition: box-shadow 0.2s;
  }

  .category-card:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }

  .category-icon {
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--gray-100);
    border-radius: 12px;
  }

  .category-name {
    font-weight: 500;
  }
</style>
```

**Step 2: Verify**

Run: `cd frontend && npm run dev`
Expected: Home page shows hero banner and category cards

**Step 3: Commit**

```bash
git add frontend/src/routes/Home.svelte
git commit -m "feat: add home page with hero and category grid"
```

---

### Task 13: Product Catalog Page

**Files:**
- Create: `frontend/src/routes/Catalog.svelte`
- Create: `frontend/src/components/ProductCard.svelte`
- Create: `frontend/src/components/SidebarFilters.svelte`

**Step 1: Write `ProductCard.svelte`**

```svelte
<script>
  import { Link } from 'svelte-routing'
  import { formatPrice, getStockStatus, getStockClass } from '../lib/utils.js'
  import { cart } from '../lib/stores.js'

  export let product

  function addToCart(e) {
    e.preventDefault()
    e.stopPropagation()
    cart.add(product)
  }
</script>

<div class="product-card">
  <Link to={`/products/${product.id}`}>
    <div class="product-image">
      {#if product.imageUrl}
        <img src={product.imageUrl} alt={product.name} />
      {:else}
        <div class="placeholder">No Image</div>
      {/if}
    </div>
    <div class="product-info">
      <span class="category-badge">{product.categoryName}</span>
      <h3>{product.name}</h3>
      <p class="manufacturer">{product.manufacturer || 'Unknown'}</p>
      <div class="rating">
        <span class="stars">★★★★★</span>
        <span class="count">(0)</span>
      </div>
      <div class="product-footer">
        <span class="price">{formatPrice(product.price)}</span>
        <span class="stock stock-{getStockClass(product.stockQuantity)}">{getStockStatus(product.stockQuantity)}</span>
      </div>
    </div>
  </Link>
  <button class="btn-add" on:click={addToCart}>Add to Cart</button>
</div>

<style>
  .product-card {
    border: 1px solid var(--gray-200);
    border-radius: 12px;
    overflow: hidden;
    transition: box-shadow 0.2s;
    background: white;
  }

  .product-card:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }

  .product-card :global(a) {
    text-decoration: none;
    color: inherit;
    display: block;
  }

  .product-image {
    aspect-ratio: 4/3;
    background: var(--gray-100);
    overflow: hidden;
  }

  .product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--gray-800);
  }

  .product-info {
    padding: 1rem;
  }

  .category-badge {
    display: inline-block;
    background: var(--gray-100);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    color: var(--gray-800);
  }

  h3 {
    font-size: 1rem;
    margin: 0.5rem 0 0.25rem;
  }

  .manufacturer {
    font-size: 0.875rem;
    color: var(--gray-800);
    margin-bottom: 0.5rem;
  }

  .rating {
    font-size: 0.875rem;
    margin-bottom: 0.75rem;
  }

  .stars { color: var(--warning); }
  .count { color: var(--gray-800); }

  .product-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .price {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--primary);
  }

  .stock {
    font-size: 0.75rem;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
  }

  .stock-in { background: #dcfce7; color: var(--success); }
  .stock-low { background: #fef3c7; color: #92400e; }
  .stock-out { background: #fee2e2; color: var(--danger); }

  .btn-add {
    width: calc(100% - 2rem);
    margin: 0 1rem 1rem;
    padding: 0.5rem;
    background: var(--primary);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
  }

  .btn-add:hover {
    background: var(--primary-dark);
  }
</style>
```

**Step 2: Write `SidebarFilters.svelte`**

```svelte
<script>
  import { createEventDispatcher } from 'svelte'

  export let categories = []
  export let manufacturers = []
  export let filters

  const dispatch = createEventDispatcher()

  function update() {
    dispatch('change', filters)
  }

  function toggleCategory(catId) {
    const idx = filters.categoryIds.indexOf(catId)
    if (idx > -1) {
      filters.categoryIds.splice(idx, 1)
    } else {
      filters.categoryIds.push(catId)
    }
    filters = filters
    update()
  }

  function toggleManufacturer(mfg) {
    const idx = filters.manufacturers.indexOf(mfg)
    if (idx > -1) {
      filters.manufacturers.splice(idx, 1)
    } else {
      filters.manufacturers.push(mfg)
    }
    filters = filters
    update()
  }
</script>

<aside class="sidebar">
  <div class="filter-section">
    <h4>Price Range</h4>
    <div class="price-inputs">
      <input type="number" placeholder="Min" bind:value={filters.minPrice} on:change={update} />
      <span>-</span>
      <input type="number" placeholder="Max" bind:value={filters.maxPrice} on:change={update} />
    </div>
  </div>

  <div class="filter-section">
    <h4>Category</h4>
    <label class="checkbox-label">
      <input type="checkbox" checked={filters.categoryIds.length === 0} on:change={() => { filters.categoryIds = []; update(); }} />
      All
    </label>
    {#each categories as cat}
      <label class="checkbox-label">
        <input type="checkbox" checked={filters.categoryIds.includes(cat.id)} on:change={() => toggleCategory(cat.id)} />
        {cat.name}
      </label>
    {/each}
  </div>

  <div class="filter-section">
    <h4>Manufacturer</h4>
    <label class="checkbox-label">
      <input type="checkbox" checked={filters.manufacturers.length === 0} on:change={() => { filters.manufacturers = []; update(); }} />
      All
    </label>
    {#each manufacturers as mfg}
      <label class="checkbox-label">
        <input type="checkbox" checked={filters.manufacturers.includes(mfg)} on:change={() => toggleManufacturer(mfg)} />
        {mfg}
      </label>
    {/each}
  </div>
</aside>

<style>
  .sidebar {
    width: 240px;
    flex-shrink: 0;
  }

  .filter-section {
    margin-bottom: 1.5rem;
  }

  .filter-section h4 {
    font-size: 0.875rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .price-inputs {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .price-inputs input {
    width: 80px;
    padding: 0.375rem;
    border: 1px solid var(--gray-200);
    border-radius: 4px;
  }

  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.375rem 0;
    font-size: 0.875rem;
    cursor: pointer;
  }

  .checkbox-label input {
    cursor: pointer;
  }
</style>
```

**Step 3: Write `Catalog.svelte`**

```svelte
<script>
  import { onMount } from 'svelte'
  import { location } from 'svelte-routing'
  import api from '../lib/api.js'
  import ProductCard from '../components/ProductCard.svelte'
  import SidebarFilters from '../components/SidebarFilters.svelte'

  let products = []
  let categories = []
  let manufacturers = []
  let loading = true

  let filters = {
    search: '',
    categoryIds: [],
    manufacturers: [],
    minPrice: null,
    maxPrice: null
  }

  $: {
    const params = new URLSearchParams($location.search)
    filters.search = params.get('search') || ''
    const catId = params.get('categoryId')
    filters.categoryIds = catId ? [parseInt(catId)] : []
    loadProducts()
  }

  async function loadProducts() {
    loading = true
    const params = new URLSearchParams()
    if (filters.search) params.append('search', filters.search)
    if (filters.categoryIds.length === 1) params.append('categoryId', filters.categoryIds[0])
    if (filters.minPrice) params.append('minPrice', filters.minPrice)
    if (filters.maxPrice) params.append('maxPrice', filters.maxPrice)

    const res = await api.get(`/products?${params.toString()}`)
    products = res.data
    loading = false
  }

  onMount(async () => {
    const [catsRes, prodsRes] = await Promise.all([
      api.get('/categories'),
      api.get('/products')
    ])
    categories = catsRes.data
    products = prodsRes.data
    const mfgSet = new Set()
    products.forEach(p => { if (p.manufacturer) mfgSet.add(p.manufacturer) })
    manufacturers = Array.from(mfgSet).sort()
    loading = false
  })

  function handleFilterChange() {
    loadProducts()
  }
</script>

<div class="catalog-page">
  <div class="catalog-header">
    <h1>Product Catalog</h1>
    <p>{products.length} products found</p>
  </div>

  <div class="catalog-body">
    <SidebarFilters
      {categories}
      {manufacturers}
      bind:filters
      on:change={handleFilterChange}
    />

    <div class="product-grid">
      {#if loading}
        <p>Loading...</p>
      {:else}
        {#each products as product}
          <ProductCard {product} />
        {:else}
          <p>No products found.</p>
        {/each}
      {/if}
    </div>
  </div>
</div>

<style>
  .catalog-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .catalog-header {
    margin-bottom: 1.5rem;
  }

  .catalog-header h1 {
    font-size: 1.5rem;
    margin-bottom: 0.25rem;
  }

  .catalog-header p {
    color: var(--gray-800);
    font-size: 0.875rem;
  }

  .catalog-body {
    display: flex;
    gap: 2rem;
  }

  .product-grid {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
  }
</style>
```

**Step 4: Verify**

Run: `cd frontend && npm run dev`
Go to `/products`
Expected: Sidebar with filters, product grid with cards

**Step 5: Commit**

```bash
git add frontend/src/components/ProductCard.svelte
frontend/src/components/SidebarFilters.svelte
frontend/src/routes/Catalog.svelte
git commit -m "feat: add product catalog with filters and product cards"
```

---

### Task 14: Product Detail Page

**Files:**
- Create: `frontend/src/routes/ProductDetail.svelte`

**Step 1: Write `ProductDetail.svelte`**

```svelte
<script>
  import { onMount } from 'svelte'
  import { Link } from 'svelte-routing'
  import api from '../lib/api.js'
  import { cart } from '../lib/stores.js'
  import { formatPrice, getStockStatus, getStockClass } from '../lib/utils.js'

  export let id

  let product = null
  let loading = true

  onMount(async () => {
    const res = await api.get(`/products/${id}`)
    product = res.data
    loading = false
  })

  function addToCart() {
    cart.add(product)
  }
</script>

{#if loading}
  <div class="loading">Loading...</div>
{:else if product}
  <div class="product-detail">
    <div class="breadcrumb">
      <Link to="/">Home</Link> / <Link to="/products">Products</Link> / <span>{product.name}</span>
    </div>

    <div class="detail-grid">
      <div class="image-section">
        {#if product.imageUrl}
          <img src={product.imageUrl} alt={product.name} />
        {:else}
          <div class="placeholder">No Image</div>
        {/if}
      </div>

      <div class="info-section">
        <span class="category-badge">{product.categoryName}</span>
        <h1>{product.name}</h1>
        <p class="manufacturer">{product.manufacturer || 'Unknown Manufacturer'}</p>

        <div class="price-section">
          <span class="price">{formatPrice(product.price)}</span>
          <span class="stock stock-{getStockClass(product.stockQuantity)}">{getStockStatus(product.stockQuantity)}</span>
        </div>

        <div class="actions">
          <button class="btn btn-primary" on:click={addToCart}>Add to Cart</button>
          {#if product.datasheetUrl}
            <a href={product.datasheetUrl} target="_blank" class="btn btn-secondary">Download Datasheet (PDF)</a>
          {/if}
        </div>
      </div>
    </div>

    <div class="specs-section">
      <h2>Technical Specifications</h2>
      <table class="specs-table">
        <tbody>
          {#each Object.entries(product.specs || {}) as [key, value]}
            <tr>
              <td class="spec-key">{key}</td>
              <td class="spec-value">{value}</td>
            </tr>
          {:else}
            <tr><td colspan="2">No specifications available.</td></tr>
          {/each}
        </tbody>
      </table>
    </div>
  </div>
{/if}

<style>
  .product-detail {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .breadcrumb {
    font-size: 0.875rem;
    color: var(--gray-800);
    margin-bottom: 1.5rem;
  }

  .breadcrumb :global(a) {
    color: var(--primary);
    text-decoration: none;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 3rem;
    margin-bottom: 3rem;
  }

  .image-section img {
    width: 100%;
    border-radius: 12px;
    object-fit: cover;
  }

  .placeholder {
    aspect-ratio: 4/3;
    background: var(--gray-100);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    color: var(--gray-800);
  }

  .category-badge {
    display: inline-block;
    background: var(--gray-100);
    padding: 0.25rem 0.75rem;
    border-radius: 4px;
    font-size: 0.75rem;
    margin-bottom: 0.5rem;
  }

  h1 {
    font-size: 2rem;
    margin-bottom: 0.5rem;
  }

  .manufacturer {
    color: var(--gray-800);
    margin-bottom: 1.5rem;
  }

  .price-section {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .price {
    font-size: 2rem;
    font-weight: 700;
    color: var(--primary);
  }

  .stock {
    padding: 0.375rem 0.75rem;
    border-radius: 4px;
    font-size: 0.875rem;
  }

  .stock-in { background: #dcfce7; color: var(--success); }
  .stock-low { background: #fef3c7; color: #92400e; }
  .stock-out { background: #fee2e2; color: var(--danger); }

  .actions {
    display: flex;
    gap: 1rem;
  }

  .btn {
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-weight: 500;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
  }

  .btn-primary {
    background: var(--primary);
    color: white;
  }

  .btn-secondary {
    background: white;
    color: var(--gray-900);
    border: 1px solid var(--gray-200);
  }

  .specs-section h2 {
    font-size: 1.25rem;
    margin-bottom: 1rem;
  }

  .specs-table {
    width: 100%;
    border-collapse: collapse;
  }

  .specs-table td {
    padding: 0.75rem 1rem;
    border-bottom: 1px solid var(--gray-200);
  }

  .spec-key {
    width: 40%;
    color: var(--gray-800);
    font-weight: 500;
  }

  .spec-value {
    color: var(--gray-900);
  }
</style>
```

**Step 2: Verify**

Run: `cd frontend && npm run dev`
Click a product card
Expected: Product detail page with image, specs table, add to cart button

**Step 3: Commit**

```bash
git add frontend/src/routes/ProductDetail.svelte
git commit -m "feat: add product detail page with specs and cart action"
```

---

## Phase 7: Frontend - Comparison & Cart

### Task 15: Product Comparison Page

**Files:**
- Create: `frontend/src/routes/Compare.svelte`
- Create: `frontend/src/components/ComparisonTable.svelte`

**Step 1: Write `ComparisonTable.svelte`**

```svelte
<script>
  import { formatPrice } from '../lib/utils.js'

  export let products = []

  $: allKeys = [...new Set(products.flatMap(p => Object.keys(p.specs || {})))]
</script>

<div class="comparison-table-wrapper">
  <table class="comparison-table">
    <thead>
      <tr>
        <th class="attr-header">Attribute</th>
        {#each products as product}
          <th class="product-header">
            <div class="product-col">
              {#if product.imageUrl}
                <img src={product.imageUrl} alt={product.name} />
              {/if}
              <span class="cat-badge">{product.categoryName}</span>
              <span class="p-name">{product.name}</span>
              <span class="p-mfg">{product.manufacturer}</span>
            </div>
          </th>
        {/each}
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="attr-cell">Price</td>
        {#each products as p}
          <td class="value-cell price">{formatPrice(p.price)}</td>
        {/each}
      </tr>
      <tr>
        <td class="attr-cell">Stock</td>
        {#each products as p}
          <td class="value-cell">{p.stockQuantity} units</td>
        {/each}
      </tr>
      <tr>
        <td class="attr-cell">Rating</td>
        {#each products as p}
          <td class="value-cell">4.0 (0)</td>
        {/each}
      </tr>
      {#each allKeys as key}
        <tr>
          <td class="attr-cell">{key}</td>
          {#each products as p}
            <td class="value-cell">{p.specs?.[key] || '-'}</td>
          {/each}
        </tr>
      {/each}
    </tbody>
  </table>
</div>

<style>
  .comparison-table-wrapper {
    overflow-x: auto;
  }

  .comparison-table {
    width: 100%;
    border-collapse: collapse;
    min-width: 600px;
  }

  th, td {
    border: 1px solid var(--gray-200);
    padding: 1rem;
    text-align: center;
  }

  .attr-header, .attr-cell {
    text-align: left;
    font-weight: 500;
    background: var(--gray-100);
    width: 150px;
  }

  .product-col {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }

  .product-col img {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: 8px;
  }

  .cat-badge {
    font-size: 0.75rem;
    background: var(--gray-100);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
  }

  .p-name {
    font-weight: 600;
  }

  .p-mfg {
    font-size: 0.875rem;
    color: var(--gray-800);
  }

  .value-cell.price {
    color: var(--primary);
    font-weight: 700;
    font-size: 1.25rem;
  }
</style>
```

**Step 2: Write `Compare.svelte`**

```svelte
<script>
  import { onMount } from 'svelte'
  import api from '../lib/api.js'
  import ComparisonTable from '../components/ComparisonTable.svelte'

  let products = []
  let allProducts = []
  let selectedIds = [null, null, null]

  onMount(async () => {
    const res = await api.get('/products')
    allProducts = res.data
  })

  function addProduct(index) {
    const id = selectedIds[index]
    if (!id) return
    const p = allProducts.find(x => x.id === parseInt(id))
    if (p && !products.find(x => x.id === p.id)) {
      products[index] = p
      products = [...products]
    }
  }

  function removeProduct(index) {
    products.splice(index, 1)
    products = [...products]
    selectedIds[index] = null
    selectedIds = [...selectedIds]
  }
</script>

<div class="compare-page">
  <div class="compare-header">
    <h1>Product Comparison</h1>
    <p>Compare technical specifications side by side</p>
  </div>

  <div class="selector-row">
    {#each [0, 1, 2] as i}
      <div class="selector">
        {#if products[i]}
          <div class="selected-product">
            <span>{products[i].name}</span>
            <button class="btn-remove" on:click={() => removeProduct(i)}>×</button>
          </div>
        {:else}
          <select bind:value={selectedIds[i]} on:change={() => addProduct(i)}>
            <option value={null}>Select a product</option>
            {#each allProducts as p}
              <option value={p.id}>{p.name}</option>
            {/each}
          </select>
        {/if}
      </div>
    {/each}
    <div class="add-placeholder">
      {#if products.length < 3}
        <span>+</span>
      {/if}
    </div>
  </div>

  {#if products.length > 0}
    <ComparisonTable {products} />
  {:else}
    <p class="empty">Select products above to compare their specifications.</p>
  {/if}
</div>

<style>
  .compare-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .compare-header {
    margin-bottom: 2rem;
  }

  .compare-header h1 {
    font-size: 1.5rem;
    margin-bottom: 0.25rem;
  }

  .compare-header p {
    color: var(--gray-800);
  }

  .selector-row {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
    align-items: center;
  }

  .selector {
    flex: 1;
  }

  .selector select {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
  }

  .selected-product {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 1rem;
    background: var(--gray-100);
    border-radius: 8px;
    font-weight: 500;
  }

  .btn-remove {
    background: none;
    border: none;
    font-size: 1.25rem;
    cursor: pointer;
    color: var(--gray-800);
  }

  .add-placeholder {
    width: 80px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px dashed var(--gray-200);
    border-radius: 8px;
    color: var(--gray-800);
    font-size: 1.5rem;
  }

  .empty {
    text-align: center;
    padding: 3rem;
    color: var(--gray-800);
  }
</style>
```

**Step 3: Verify**

Run: `cd frontend && npm run dev`
Go to `/compare`
Expected: Dropdown selectors, comparison matrix when products selected

**Step 4: Commit**

```bash
git add frontend/src/components/ComparisonTable.svelte
frontend/src/routes/Compare.svelte
git commit -m "feat: add product comparison page with spec matrix"
```

---

### Task 16: Shopping Cart Page

**Files:**
- Create: `frontend/src/routes/Cart.svelte`

**Step 1: Write `Cart.svelte`**

```svelte
<script>
  import { Link, navigate } from 'svelte-routing'
  import { cart } from '../lib/stores.js'
  import { auth } from '../lib/stores.js'
  import { formatPrice } from '../lib/utils.js'
  import api from '../lib/api.js'

  let items = []
  cart.subscribe(v => { items = v })

  $: total = items.reduce((sum, i) => sum + i.price * i.quantity, 0)

  function updateQty(id, qty) {
    cart.updateQuantity(id, parseInt(qty))
  }

  async function checkout() {
    if (!$auth) {
      navigate('/login')
      return
    }
    try {
      await api.post('/orders', {
        items: items.map(i => ({ productId: i.id, quantity: i.quantity }))
      })
      cart.clear()
      alert('Order placed successfully!')
      navigate('/')
    } catch (e) {
      alert(e.response?.data?.message || 'Checkout failed')
    }
  }
</script>

<div class="cart-page">
  <h1>Shopping Cart</h1>

  {#if items.length === 0}
    <div class="empty-cart">
      <p>Your cart is empty.</p>
      <Link to="/products" class="btn btn-primary">Continue Shopping</Link>
    </div>
  {:else}
    <div class="cart-layout">
      <div class="cart-items">
        {#each items as item}
          <div class="cart-item">
            {#if item.imageUrl}
              <img src={item.imageUrl} alt={item.name} />
            {:else}
              <div class="placeholder">No Image</div>
            {/if}
            <div class="item-info">
              <h3>{item.name}</h3>
              <p class="mfg">{item.manufacturer}</p>
            </div>
            <div class="item-qty">
              <input
                type="number"
                min="1"
                max={item.stockQuantity}
                value={item.quantity}
                on:change={(e) => updateQty(item.id, e.target.value)}
              />
            </div>
            <div class="item-price">
              {formatPrice(item.price * item.quantity)}
            </div>
            <button class="btn-remove" on:click={() => cart.remove(item.id)}>×</button>
          </div>
        {/each}
      </div>

      <div class="cart-summary">
        <h2>Order Summary</h2>
        <div class="summary-row">
          <span>Subtotal</span>
          <span>{formatPrice(total)}</span>
        </div>
        <div class="summary-row">
          <span>Shipping</span>
          <span>Free</span>
        </div>
        <div class="summary-row total">
          <span>Total</span>
          <span>{formatPrice(total)}</span>
        </div>
        <button class="btn btn-primary btn-checkout" on:click={checkout}>
          {$auth ? 'Place Order' : 'Login to Checkout'}
        </button>
      </div>
    </div>
  {/if}
</div>

<style>
  .cart-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  h1 { margin-bottom: 1.5rem; }

  .empty-cart {
    text-align: center;
    padding: 3rem;
  }

  .empty-cart :global(.btn) {
    display: inline-block;
    margin-top: 1rem;
    padding: 0.75rem 1.5rem;
    background: var(--primary);
    color: white;
    text-decoration: none;
    border-radius: 8px;
  }

  .cart-layout {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
  }

  .cart-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
    margin-bottom: 1rem;
  }

  .cart-item img, .placeholder {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    background: var(--gray-100);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.75rem;
    color: var(--gray-800);
  }

  .item-info { flex: 1; }

  .item-info h3 {
    font-size: 1rem;
    margin-bottom: 0.25rem;
  }

  .mfg {
    font-size: 0.875rem;
    color: var(--gray-800);
  }

  .item-qty input {
    width: 60px;
    padding: 0.375rem;
    border: 1px solid var(--gray-200);
    border-radius: 4px;
  }

  .item-price {
    font-weight: 600;
    min-width: 80px;
    text-align: right;
  }

  .btn-remove {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: var(--gray-800);
  }

  .cart-summary {
    background: var(--gray-100);
    padding: 1.5rem;
    border-radius: 12px;
    height: fit-content;
  }

  .cart-summary h2 {
    font-size: 1.125rem;
    margin-bottom: 1rem;
  }

  .summary-row {
    display: flex;
    justify-content: space-between;
    padding: 0.75rem 0;
    border-bottom: 1px solid var(--gray-200);
  }

  .summary-row.total {
    font-weight: 700;
    font-size: 1.125rem;
    border-bottom: none;
  }

  .btn-checkout {
    width: 100%;
    margin-top: 1rem;
    padding: 0.875rem;
    background: var(--primary);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
  }
</style>
```

**Step 2: Verify**

Run: `cd frontend && npm run dev`
Add items to cart, go to `/cart`
Expected: Cart items list, quantity controls, total calculation, checkout button

**Step 3: Commit**

```bash
git add frontend/src/routes/Cart.svelte
git commit -m "feat: add shopping cart with checkout flow"
```

---

## Phase 8: Frontend - Admin

### Task 17: Admin Inventory Management

**Files:**
- Create: `frontend/src/routes/Admin.svelte`
- Create: `frontend/src/components/StatsCard.svelte`
- Create: `frontend/src/components/LowStockAlert.svelte`
- Create: `frontend/src/components/InventoryTable.svelte`

**Step 1: Write `StatsCard.svelte`**

```svelte
<script>
  export let title
  export let value
  export let icon = ''
  export let color = 'blue'
</script>

<div class="stats-card">
  <div class="stats-info">
    <p class="stats-title">{title}</p>
    <p class="stats-value">{value}</p>
  </div>
  <div class="stats-icon {color}">
    {@html icon}
  </div>
</div>

<style>
  .stats-card {
    background: white;
    border: 1px solid var(--gray-200);
    border-radius: 12px;
    padding: 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .stats-title {
    font-size: 0.875rem;
    color: var(--gray-800);
    margin-bottom: 0.5rem;
  }

  .stats-value {
    font-size: 2rem;
    font-weight: 700;
  }

  .stats-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
  }

  .stats-icon.blue { background: #dbeafe; color: var(--primary); }
  .stats-icon.green { background: #dcfce7; color: var(--success); }
  .stats-icon.purple { background: #f3e8ff; color: #9333ea; }
  .stats-icon.red { background: #fee2e2; color: var(--danger); }
</style>
```

**Step 2: Write `LowStockAlert.svelte`**

```svelte
<script>
  export let count = 0
</script>

{#if count > 0}
  <div class="alert">
    <span class="alert-icon">!</span>
    <div>
      <strong>Low Stock Alert</strong>
      <p>{count} products are running low on stock. Consider reordering soon.</p>
    </div>
  </div>
{/if}

<style>
  .alert {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    background: #fff7ed;
    border: 1px solid #fed7aa;
    border-radius: 8px;
    padding: 1rem;
    margin-bottom: 1.5rem;
  }

  .alert-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    background: #f97316;
    color: white;
    border-radius: 50%;
    font-size: 0.875rem;
    font-weight: 700;
    flex-shrink: 0;
  }

  .alert strong {
    display: block;
    margin-bottom: 0.25rem;
  }

  .alert p {
    font-size: 0.875rem;
    color: #9a3412;
  }
</style>
```

**Step 3: Write `InventoryTable.svelte`**

```svelte
<script>
  import { formatPrice } from '../lib/utils.js'
  import { createEventDispatcher } from 'svelte'

  export let products = []

  const dispatch = createEventDispatcher()

  function getStatus(quantity) {
    if (quantity === 0) return 'Out of Stock'
    if (quantity < 10) return 'Low Stock'
    return 'In Stock'
  }

  function getStatusClass(quantity) {
    if (quantity === 0) return 'out'
    if (quantity < 10) return 'low'
    return 'in'
  }
</script>

<div class="table-wrapper">
  <table class="inventory-table">
    <thead>
      <tr>
        <th>Product</th>
        <th>Category</th>
        <th>Manufacturer</th>
        <th>Price</th>
        <th>Stock</th>
        <th>Status</th>
        <th>Value</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each products as product}
        <tr>
          <td class="product-cell">
            {#if product.imageUrl}
              <img src={product.imageUrl} alt={product.name} />
            {:else}
              <div class="img-placeholder"></div>
            {/if}
            <div>
              <div class="p-name">{product.name}</div>
              <div class="p-id">ID: {product.id}</div>
            </div>
          </td>
          <td>{product.categoryName}</td>
          <td>{product.manufacturer || '-'}</td>
          <td>{formatPrice(product.price)}</td>
          <td>{product.stockQuantity}</td>
          <td>
            <span class="status status-{getStatusClass(product.stockQuantity)}">{getStatus(product.stockQuantity)}</span>
          </td>
          <td>{formatPrice(product.price * product.stockQuantity)}</td>
          <td class="actions">
            <button class="btn-icon" on:click={() => dispatch('edit', product)} title="Edit">Edit</button>
            <button class="btn-icon" on:click={() => dispatch('delete', product)} title="Delete">Del</button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>

<style>
  .table-wrapper {
    overflow-x: auto;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
  }

  .inventory-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.875rem;
  }

  th, td {
    padding: 0.75rem 1rem;
    text-align: left;
    border-bottom: 1px solid var(--gray-200);
  }

  th {
    background: var(--gray-100);
    font-weight: 600;
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .product-cell {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .product-cell img, .img-placeholder {
    width: 40px;
    height: 40px;
    border-radius: 6px;
    object-fit: cover;
    background: var(--gray-100);
  }

  .p-name { font-weight: 600; }

  .p-id {
    font-size: 0.75rem;
    color: var(--gray-800);
  }

  .status {
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: 500;
  }

  .status-in { background: #dcfce7; color: var(--success); }
  .status-low { background: #fef3c7; color: #92400e; }
  .status-out { background: #fee2e2; color: var(--danger); }

  .actions {
    display: flex;
    gap: 0.5rem;
  }

  .btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    border-radius: 4px;
  }

  .btn-icon:hover {
    background: var(--gray-100);
  }
</style>
```

**Step 4: Write `Admin.svelte`**

```svelte
<script>
  import { onMount } from 'svelte'
  import { navigate } from 'svelte-routing'
  import { auth } from '../lib/stores.js'
  import api from '../lib/api.js'
  import StatsCard from '../components/StatsCard.svelte'
  import LowStockAlert from '../components/LowStockAlert.svelte'
  import InventoryTable from '../components/InventoryTable.svelte'
  import { formatPrice } from '../lib/utils.js'

  let products = []
  let loading = true

  $: totalProducts = products.length
  $: totalStock = products.reduce((s, p) => s + p.stockQuantity, 0)
  $: totalValue = products.reduce((s, p) => s + p.price * p.stockQuantity, 0)
  $: lowStockCount = products.filter(p => p.stockQuantity > 0 && p.stockQuantity < 10).length

  onMount(async () => {
    if (!$auth || $auth.role !== 'ADMIN') {
      navigate('/login')
      return
    }
    const res = await api.get('/products')
    products = res.data
    loading = false
  })

  let searchQuery = ''

  $: filteredProducts = products.filter(p =>
    !searchQuery || p.name.toLowerCase().includes(searchQuery.toLowerCase())
  )

  async function handleEdit(product) {
    // Will implement ProductFormModal in Task 18
  }

  async function handleDelete(product) {
    if (!confirm(`Delete ${product.name}?`)) return
    await api.delete(`/products/${product.id}`)
    products = products.filter(p => p.id !== product.id)
  }
</script>

{#if loading}
  <div class="loading">Loading...</div>
{:else}
  <div class="admin-page">
    <div class="admin-header">
      <h1>Inventory Management</h1>
      <button class="btn btn-primary">+ Add Product</button>
    </div>

    <div class="stats-grid">
      <StatsCard title="Total Products" value={totalProducts} icon="" color="blue" />
      <StatsCard title="Total Stock Units" value={totalStock} icon="" color="green" />
      <StatsCard title="Inventory Value" value={formatPrice(totalValue)} icon="" color="purple" />
      <StatsCard title="Low Stock Items" value={lowStockCount} icon="" color="red" />
    </div>

    <LowStockAlert count={lowStockCount} />

    <div class="inventory-section">
      <div class="section-header">
        <input
          type="text"
          placeholder="Search products..."
          bind:value={searchQuery}
          class="search-input"
        />
      </div>
      <InventoryTable products={filteredProducts} on:edit on:delete />
    </div>
  </div>
{/if}

<style>
  .admin-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  .admin-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
  }

  .admin-header h1 {
    font-size: 1.5rem;
  }

  .btn-primary {
    padding: 0.75rem 1.5rem;
    background: var(--primary);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .inventory-section {
    background: white;
    border-radius: 8px;
  }

  .section-header {
    margin-bottom: 1rem;
  }

  .search-input {
    width: 100%;
    max-width: 300px;
    padding: 0.5rem 1rem;
    border: 1px solid var(--gray-200);
    border-radius: 8px;
  }
</style>
```

**Step 5: Verify**

Run: `cd frontend && npm run dev`
Login as admin (register a user then manually update role to ADMIN in DB)
Go to `/admin`
Expected: Stats cards, low stock alert, product inventory table

**Step 6: Commit**

```bash
git add frontend/src/components/StatsCard.svelte
frontend/src/components/LowStockAlert.svelte
frontend/src/components/InventoryTable.svelte
frontend/src/routes/Admin.svelte
git commit -m "feat: add admin inventory management with stats and alerts"
```

---

### Task 18: Add/Edit Product Modal

**Files:**
- Create: `frontend/src/components/ProductFormModal.svelte`

**Step 1: Write `ProductFormModal.svelte`**

```svelte
<script>
  import { createEventDispatcher, onMount } from 'svelte'
  import api from '../lib/api.js'

  export let product = null
  export let onClose = () => {}

  const dispatch = createEventDispatcher()

  let categories = []
  let form = {
    name: '',
    sku: '',
    price: '',
    stockQuantity: '',
    categoryId: '',
    manufacturer: '',
    imageUrl: '',
    datasheetUrl: '',
    specs: []
  }

  onMount(async () => {
    const res = await api.get('/categories')
    categories = res.data
    if (product) {
      form = {
        name: product.name,
        sku: product.sku,
        price: String(product.price),
        stockQuantity: String(product.stockQuantity),
        categoryId: String(product.categoryId || ''),
        manufacturer: product.manufacturer || '',
        imageUrl: product.imageUrl || '',
        datasheetUrl: product.datasheetUrl || '',
        specs: Object.entries(product.specs || {}).map(([k, v]) => ({ key: k, value: v }))
      }
    }
  })

  function addSpec() {
    form.specs = [...form.specs, { key: '', value: '' }]
  }

  function removeSpec(index) {
    form.specs.splice(index, 1)
    form.specs = [...form.specs]
  }

  async function handleSubmit() {
    const data = {
      name: form.name,
      sku: form.sku,
      price: parseFloat(form.price),
      stockQuantity: parseInt(form.stockQuantity),
      categoryId: parseInt(form.categoryId),
      manufacturer: form.manufacturer,
      imageUrl: form.imageUrl,
      datasheetUrl: form.datasheetUrl,
      specs: Object.fromEntries(form.specs.filter(s => s.key).map(s => [s.key, s.value]))
    }

    try {
      if (product) {
        await api.put(`/products/${product.id}`, data)
      } else {
        await api.post('/products', data)
      }
      dispatch('save')
      onClose()
    } catch (e) {
      alert(e.response?.data?.message || 'Save failed')
    }
  }
</script>

<div class="modal-backdrop" on:click={onClose}>
  <div class="modal" on:click|stopPropagation>
    <div class="modal-header">
      <h2>{product ? 'Edit Product' : 'Add Product'}</h2>
      <button class="btn-close" on:click={onClose}>×</button>
    </div>

    <form on:submit|preventDefault={handleSubmit} class="modal-body">
      <div class="form-grid">
        <label>
          Name
          <input type="text" bind:value={form.name} required />
        </label>
        <label>
          SKU
          <input type="text" bind:value={form.sku} required />
        </label>
        <label>
          Price
          <input type="number" step="0.01" bind:value={form.price} required />
        </label>
        <label>
          Stock Quantity
          <input type="number" bind:value={form.stockQuantity} required />
        </label>
        <label>
          Category
          <select bind:value={form.categoryId} required>
            <option value="">Select...</option>
            {#each categories as cat}
              <option value={cat.id}>{cat.name}</option>
            {/each}
          </select>
        </label>
        <label>
          Manufacturer
          <input type="text" bind:value={form.manufacturer} />
        </label>
        <label>
          Image URL
          <input type="text" bind:value={form.imageUrl} />
        </label>
        <label>
          Datasheet URL
          <input type="text" bind:value={form.datasheetUrl} />
        </label>
      </div>

      <div class="specs-section">
        <div class="specs-header">
          <h3>Technical Specifications</h3>
          <button type="button" class="btn-add-spec" on:click={addSpec}>+ Add Spec</button>
        </div>
        {#each form.specs as spec, i}
          <div class="spec-row">
            <input type="text" placeholder="Key" bind:value={spec.key} />
            <input type="text" placeholder="Value" bind:value={spec.value} />
            <button type="button" class="btn-remove-spec" on:click={() => removeSpec(i)}>×</button>
          </div>
        {/each}
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-cancel" on:click={onClose}>Cancel</button>
        <button type="submit" class="btn btn-primary">Save Product</button>
      </div>
    </form>
  </div>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 200;
  }

  .modal {
    background: white;
    border-radius: 12px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid var(--gray-200);
  }

  .modal-header h2 {
    font-size: 1.25rem;
  }

  .btn-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: var(--gray-800);
  }

  .modal-body {
    padding: 1.5rem;
  }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
  }

  label input, label select {
    display: block;
    width: 100%;
    margin-top: 0.25rem;
    padding: 0.5rem;
    border: 1px solid var(--gray-200);
    border-radius: 6px;
  }

  .specs-section {
    margin-top: 1.5rem;
  }

  .specs-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
  }

  .specs-header h3 {
    font-size: 1rem;
  }

  .btn-add-spec {
    font-size: 0.875rem;
    background: var(--primary);
    color: white;
    border: none;
    padding: 0.375rem 0.75rem;
    border-radius: 6px;
    cursor: pointer;
  }

  .spec-row {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
  }

  .spec-row input {
    flex: 1;
    padding: 0.375rem;
    border: 1px solid var(--gray-200);
    border-radius: 4px;
  }

  .btn-remove-spec {
    background: none;
    border: none;
    font-size: 1.25rem;
    cursor: pointer;
    color: var(--danger);
    padding: 0 0.5rem;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid var(--gray-200);
  }

  .btn {
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-weight: 500;
  }

  .btn-cancel {
    background: var(--gray-100);
    color: var(--gray-800);
  }

  .btn-primary {
    background: var(--primary);
    color: white;
  }
</style>
```

**Step 2: Integrate modal into Admin.svelte**

Modify `Admin.svelte` to import and use `ProductFormModal`:

```javascript
import ProductFormModal from '../components/ProductFormModal.svelte'
let showModal = false
let editingProduct = null
```

Show/hide modal on Add/Edit clicks, refresh product list on save.

**Step 3: Verify**

Run: `cd frontend && npm run dev`
Click "Add Product" on admin page
Expected: Modal with form fields and dynamic specs

**Step 4: Commit**

```bash
git add frontend/src/components/ProductFormModal.svelte
git commit -m "feat: add product add/edit modal with dynamic specs"
```

---

## Phase 9: Integration & Polish

### Task 19: Seed Data Script

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/config/DataSeeder.java`

**Step 1: Write `DataSeeder.java`**

```java
package com.specsheetcentral.config;

import com.specsheetcentral.model.*;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(CategoryRepository categories,
                           ProductRepository products,
                           ProductSpecRepository specs,
                           UserRepository users,
                           PasswordEncoder encoder) {
        return args -> {
            if (categories.count() > 0) return;

            Category microcontrollers = new Category();
            microcontrollers.setName("Microcontrollers");
            categories.save(microcontrollers);

            Category sensors = new Category();
            sensors.setName("Sensors");
            categories.save(sensors);

            Category leds = new Category();
            leds.setName("LEDs");
            categories.save(leds);

            Category motors = new Category();
            motors.setName("Motors");
            categories.save(motors);

            Product p1 = new Product();
            p1.setName("Arduino Uno R3");
            p1.setSku("ARD-UNO-R3");
            p1.setPrice(24.99);
            p1.setStockQuantity(150);
            p1.setCategory(microcontrollers);
            p1.setManufacturer("Arduino");
            products.save(p1);

            ProductSpec s1 = new ProductSpec();
            s1.setProduct(p1);
            s1.setSpecKey("Microcontroller");
            s1.setSpecValue("ATmega328P");
            specs.save(s1);

            ProductSpec s2 = new ProductSpec();
            s2.setProduct(p1);
            s2.setSpecKey("Operating Voltage");
            s2.setSpecValue("5V");
            specs.save(s2);

            ProductSpec s3 = new ProductSpec();
            s3.setProduct(p1);
            s3.setSpecKey("Digital I/O Pins");
            s3.setSpecValue("14");
            specs.save(s3);

            ProductSpec s4 = new ProductSpec();
            s4.setProduct(p1);
            s4.setSpecKey("Analog Input Pins");
            s4.setSpecValue("6");
            specs.save(s4);

            ProductSpec s5 = new ProductSpec();
            s5.setProduct(p1);
            s5.setSpecKey("Flash Memory");
            s5.setSpecValue("32 KB");
            specs.save(s5);

            Product p2 = new Product();
            p2.setName("Raspberry Pi 4 Model B");
            p2.setSku("RPI-4B-4GB");
            p2.setPrice(55.00);
            p2.setStockQuantity(85);
            p2.setCategory(microcontrollers);
            p2.setManufacturer("Raspberry Pi");
            products.save(p2);

            ProductSpec s6 = new ProductSpec();
            s6.setProduct(p2);
            s6.setSpecKey("Processor");
            s6.setSpecValue("Quad core Cortex-A72");
            specs.save(s6);

            ProductSpec s7 = new ProductSpec();
            s7.setProduct(p2);
            s7.setSpecKey("RAM");
            s7.setSpecValue("4GB LPDDR4");
            specs.save(s7);

            ProductSpec s8 = new ProductSpec();
            s8.setProduct(p2);
            s8.setSpecKey("Operating Voltage");
            s8.setSpecValue("5V");
            specs.save(s8);

            Product p3 = new Product();
            p3.setName("DHT22 Temperature Sensor");
            p3.setSku("DHT-22-MOD");
            p3.setPrice(9.95);
            p3.setStockQuantity(200);
            p3.setCategory(sensors);
            p3.setManufacturer("Aosong");
            products.save(p3);

            ProductSpec s9 = new ProductSpec();
            s9.setProduct(p3);
            s9.setSpecKey("Temperature Range");
            s9.setSpecValue("-40 to 80 C");
            specs.save(s9);

            ProductSpec s10 = new ProductSpec();
            s10.setProduct(p3);
            s10.setSpecKey("Humidity Range");
            s10.setSpecValue("0-100% RH");
            specs.save(s10);

            ProductSpec s11 = new ProductSpec();
            s11.setProduct(p3);
            s11.setSpecKey("Accuracy");
            s11.setSpecValue("+/-0.5 C");
            specs.save(s11);

            Product p4 = new Product();
            p4.setName("WS2812B LED Strip");
            p4.setSku("LED-WS2812B-1M");
            p4.setPrice(15.99);
            p4.setStockQuantity(120);
            p4.setCategory(leds);
            p4.setManufacturer("Worldsemi");
            products.save(p4);

            ProductSpec s12 = new ProductSpec();
            s12.setProduct(p4);
            s12.setSpecKey("LEDs per meter");
            s12.setSpecValue("60");
            specs.save(s12);

            ProductSpec s13 = new ProductSpec();
            s13.setProduct(p4);
            s13.setSpecKey("Operating Voltage");
            s13.setSpecValue("5V");
            specs.save(s13);

            ProductSpec s14 = new ProductSpec();
            s14.setProduct(p4);
            s14.setSpecKey("Color");
            s14.setSpecValue("RGB");
            specs.save(s14);

            Product p5 = new Product();
            p5.setName("28BYJ-48 Stepper Motor");
            p5.setSku("MTR-28BYJ-48");
            p5.setPrice(4.99);
            p5.setStockQuantity(300);
            p5.setCategory(motors);
            p5.setManufacturer("Generic");
            products.save(p5);

            ProductSpec s15 = new ProductSpec();
            s15.setProduct(p5);
            s15.setSpecKey("Steps per Revolution");
            s15.setSpecValue("2048");
            specs.save(s15);

            ProductSpec s16 = new ProductSpec();
            s16.setProduct(p5);
            s16.setSpecKey("Operating Voltage");
            s16.setSpecValue("5V DC");
            specs.save(s16);

            ProductSpec s17 = new ProductSpec();
            s17.setProduct(p5);
            s17.setSpecKey("Torque");
            s17.setSpecValue("34.3 mN.m");
            specs.save(s17);

            Product p6 = new Product();
            p6.setName("ESP32 Dev Board");
            p6.setSku("ESP32-DEV-38P");
            p6.setPrice(12.50);
            p6.setStockQuantity(3);
            p6.setCategory(microcontrollers);
            p6.setManufacturer("Espressif");
            products.save(p6);

            ProductSpec s18 = new ProductSpec();
            s18.setProduct(p6);
            s18.setSpecKey("Microcontroller");
            s18.setSpecValue("ESP32-D0WDQ6");
            specs.save(s18);

            ProductSpec s19 = new ProductSpec();
            s19.setProduct(p6);
            s19.setSpecKey("Operating Voltage");
            s19.setSpecValue("3.3V");
            specs.save(s19);

            ProductSpec s20 = new ProductSpec();
            s20.setProduct(p6);
            s20.setSpecKey("WiFi");
            s20.setSpecValue("802.11 b/g/n");
            specs.save(s20);

            ProductSpec s21 = new ProductSpec();
            s21.setProduct(p6);
            s21.setSpecKey("Flash Memory");
            s21.setSpecValue("4 MB");
            specs.save(s21);

            User admin = new User();
            admin.setEmail("admin@specsheet.com");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            users.save(admin);

            User user = new User();
            user.setEmail("user@specsheet.com");
            user.setPasswordHash(encoder.encode("user123"));
            user.setRole(User.Role.USER);
            users.save(user);
        };
    }
}
```

**Step 2: Verify seeder**

Run: `cd backend && mvn spring-boot:run`
Drop and recreate database, restart.
Expected: Logs show seeder runs, GET /api/products returns 6 products

**Step 3: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/config/DataSeeder.java
git commit -m "feat: add seed data with sample products, specs, and users"
```

---

### Task 20: Final Verification and Polish

**Step 1: Verify backend starts cleanly**

Run: `cd backend && mvn spring-boot:run`
Expected: No errors, tables created, seeder runs

**Step 2: Verify frontend builds cleanly**

Run: `cd frontend && npm run dev`
Expected: No compile errors

**Step 3: End-to-end test**

1. Visit http://localhost:5173
2. See home page with hero and categories
3. Click "Products" → see product grid with filters
4. Click a product → see detail with specs table
5. Add items to cart
6. Go to Compare → select products → see matrix
7. Login as admin@specsheet.com / admin123
8. Go to /admin → see inventory management
9. Click "Add Product" → fill form → save
10. Go to Cart → click "Place Order" → order created

**Step 4: Commit**

```bash
git add .
git commit -m "feat: complete integration, add seed data, final polish"
```

---

## Testing Checklist

- [ ] Spring Boot starts without errors on port 8080
- [ ] PostgreSQL schema created correctly (6 tables)
- [ ] Category CRUD: POST/GET/PUT/DELETE via curl
- [ ] Product CRUD with dynamic specs works
- [ ] Product filtering: search, category, price range, manufacturer
- [ ] File upload returns valid URL
- [ ] JWT register/login returns token with role
- [ ] Admin endpoints reject non-admin users (403)
- [ ] GET products/categories open to all (no auth needed)
- [ ] Order creation reduces stock quantity atomically
- [ ] Order with insufficient stock returns error
- [ ] Frontend compiles without Svelte errors
- [ ] Home page renders hero + categories
- [ ] Catalog page renders product grid + filters
- [ ] Product detail renders specs table
- [ ] Comparison page selects products and shows matrix
- [ ] Cart persists in localStorage across reloads
- [ ] Checkout flow works end-to-end
- [ ] Admin page shows stats, alerts, inventory table

---

## Execution Handoff

**Plan complete and saved to `docs/plans/2026-04-25-specsheet-central.md`.**

**Two execution options:**

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
