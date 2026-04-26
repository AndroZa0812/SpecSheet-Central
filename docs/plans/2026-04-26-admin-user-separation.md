# Admin/User Separation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Improve admin/user separation with centralized frontend route guards and a dedicated `/api/admin` backend prefix.

**Architecture:** Create an `AdminGuard.svelte` wrapper component that blocks non-admin users from rendering admin UI entirely. On the backend, move admin-only endpoints to `/api/admin/**` and add `@PreAuthorize` annotations for defense-in-depth. Update frontend API calls accordingly.

**Tech Stack:** Svelte 5, Spring Boot 4, Spring Security, JWT (JJWT)

---

### Task 1: Create AdminGuard Svelte component

**Files:**
- Create: `frontend/src/components/AdminGuard.svelte`

**Step 1: Create the AdminGuard component**

```svelte
<script lang="ts">
  import { auth } from "$lib/stores";
  import { navigate } from "$lib/router";

  let { children }: { children: import("svelte").Snippet } = $props();

  if (!$auth || $auth.role !== "ADMIN") {
    navigate("/");
  }
</script>

{#if $auth?.role === "ADMIN"}
  {@render children()}
{/if}
```

**Step 2: Verify component compiles**

Run: `cd /home/llmserver/Dev/SpecSheet Central/frontend && npx svelte-check --tsconfig ./tsconfig.json 2>&1 | head -20`

Expected: No errors related to AdminGuard.

---

### Task 2: Update App.svelte to wrap admin route with AdminGuard

**Files:**
- Modify: `frontend/src/App.svelte`

**Step 1: Update App.svelte**

Add the import for AdminGuard, then wrap the admin Route:

In the `<script>` section, add:
```typescript
  import AdminGuard from "./components/AdminGuard.svelte";
```

In the template, replace:
```svelte
  <Route path="/admin" component={Admin} />
```
with:
```svelte
  <Route path="/admin">
    <AdminGuard>
      <Admin />
    </AdminGuard>
  </Route>
```

Wait — the `Route` component currently passes `component={...}` as a prop and renders it. We need to modify Route to support snippet children, OR change the admin route to not use the Route component's component prop.

The simplest approach: render the admin route conditionally outside the Route pattern, or modify Route.svelte to support children.

**Better approach:** Modify `Route.svelte` to support an optional `children` snippet, then use it in App.svelte.

**Step 2: Update Route.svelte to support snippet children**

Modify `frontend/src/components/Route.svelte`:

```svelte
<script lang="ts">
  import { currentPath, match } from "$lib/router";
  import type { RouteParams } from "$lib/types";

  let { path, component: Component, children, ...rest }: { path: string; component?: any; children?: import("svelte").Snippet; [key: string]: any } = $props();
  let matched: RouteParams | null = $derived(match(path, $currentPath.split("?")[0]));
</script>

{#if matched}
  {#if children}
    {@render children({ params: matched })}
  {:else if Component}
    <Component params={matched} {...rest} />
  {/if}
{/if}
```

Note: Snippets in Svelte 5 don't easily receive args from `{@render}`. Let's keep it simpler — just render `children` without params for the admin guard case:

```svelte
<script lang="ts">
  import { currentPath, match } from "$lib/router";
  import type { RouteParams } from "$lib/types";

  let { path, component: Component, children, ...rest }: { path: string; component?: any; children?: import("svelte").Snippet; [key: string]: any } = $props();
  let matched: RouteParams | null = $derived(match(path, $currentPath.split("?")[0]));
</script>

{#if matched}
  {#if children}
    {@render children()}
  {:else}
    <Component params={matched} {...rest} />
  {/if}
{/if}
```

**Step 3: Update App.svelte**

Full updated `<script>`:
```svelte
<script lang="ts">
  import { currentPath } from "$lib/router";
  import Route from "./components/Route.svelte";
  import Navbar from "./components/Navbar.svelte";
  import Home from "./routes/Home.svelte";
  import Login from "./routes/Login.svelte";
  import Register from "./routes/Register.svelte";
  import Catalog from "./routes/Catalog.svelte";
  import ProductDetail from "./routes/ProductDetail.svelte";
  import Compare from "./routes/Compare.svelte";
  import Cart from "./routes/Cart.svelte";
  import Admin from "./routes/Admin.svelte";
  import AdminGuard from "./components/AdminGuard.svelte";
  import { Toaster } from "$lib/components/ui/sonner";
</script>
```

Update the admin route line from:
```svelte
  <Route path="/admin" component={Admin} />
```
to:
```svelte
  <Route path="/admin">
    <AdminGuard>
      <Admin />
    </AdminGuard>
  </Route>
```

**Step 4: Remove the onMount guard from Admin.svelte**

In `frontend/src/routes/Admin.svelte`, remove these lines from the `<script>` block:

Remove from imports:
```typescript
  import { onMount } from "svelte";
  import { navigate } from "$lib/router";
```

Remove the onMount block:
```typescript
  onMount(() => {
    if (!$auth || $auth.role !== "ADMIN") {
      navigate("/");
      return;
    }
    loadData();
  });
```

Replace with a simple `loadData()` call using `$effect`:
```typescript
  $effect(() => {
    loadData();
  });
```

**Step 5: Verify frontend compiles**

Run: `cd /home/llmserver/Dev/SpecSheet Central/frontend && npx svelte-check --tsconfig ./tsconfig.json 2>&1 | tail -5`

Expected: No errors.

---

### Task 3: Move admin backend endpoints under /api/admin prefix

**Files:**
- Create: `backend/src/main/java/com/specsheetcentral/controller/AdminController.java`
- Modify: `backend/src/main/java/com/specsheetcentral/controller/ProductController.java`
- Modify: `backend/src/main/java/com/specsheetcentral/controller/CategoryController.java`
- Modify: `backend/src/main/java/com/specsheetcentral/controller/OrderController.java`

**Step 1: Create AdminController.java**

This new controller aggregates all admin-only write endpoints under `/api/admin`:

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.CategoryRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.service.CategoryService;
import com.specsheetcentral.service.OrderService;
import com.specsheetcentral.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    public AdminController(ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    // Products
    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/products/{id}/stock")
    public ProductResponse updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return productService.updateStock(id, quantity);
    }

    @PostMapping(value = "/products/{id}/datasheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadDatasheet(
            @PathVariable Long id,
            @RequestParam(value = "datasheetFile", required = false) MultipartFile datasheetFile,
            @RequestParam(value = "datasheetUrl", required = false) String datasheetUrl,
            @RequestParam(value = "clearDatasheet", required = false, defaultValue = "false") boolean clearDatasheet) {
        return productService.updateDatasheet(id, datasheetFile, datasheetUrl, clearDatasheet);
    }

    // Categories
    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return categoryService.create(category);
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return categoryService.update(id, category);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Orders
    @GetMapping("/orders")
    public List<OrderResponse> getAllOrders() {
        return orderService.findAll();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }
}
```

**Step 2: Remove admin-only methods from ProductController**

Remove these methods from `ProductController.java`:
- `create()` (POST)
- `update()` (PUT)
- `delete()` (DELETE)
- `updateStock()` (PATCH)
- `uploadDatasheet()` (POST datasheet)

The remaining ProductController should only have:
```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.service.ProductService;
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
            @RequestParam(required = false) List<Long> categoryId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return productService.findAll(search, categoryId, manufacturer, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }
}
```

**Step 3: Remove admin-only methods from CategoryController**

Remove `create()`, `update()`, `delete()`. Remaining:

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.model.Category;
import com.specsheetcentral.service.CategoryService;
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
}
```

**Step 4: Remove admin-only methods from OrderController**

Remove `getAll()` and `updateStatus()`. The remaining OrderController handles user-facing operations:

```java
package com.specsheetcentral.controller;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
}
```

**Step 5: Update SecurityConfig.java**

Simplify the security rules now that admin endpoints are under a dedicated prefix:

```java
package com.specsheetcentral.config;

import com.specsheetcentral.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    @Profile("!test")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").authenticated()
                    .requestMatchers("/api/orders/**").authenticated()
                    .requestMatchers("/api/reviews/**").authenticated()
                    .requestMatchers("/api/files/**").authenticated()
                    .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            throw new SecurityException("Failed to configure security filter chain", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new SecurityException("Failed to create authentication manager", e);
        }
    }
}
```

Key changes:
- Added `@EnableMethodSecurity` to enable `@PreAuthorize` on AdminController
- Replaced all per-method rules for products/categories with single `/api/admin/**` rule
- Removed `API_PRODUCTS`, `API_CATEGORIES` constants (no longer needed)

**Step 6: Verify backend compiles**

Run: `cd /home/llmserver/Dev/SpecSheet Central/backend && ./mvnw compile -q 2>&1 | tail -10`

Expected: BUILD SUCCESS

---

### Task 4: Update frontend API calls to use new /api/admin prefix

**Files:**
- Modify: `frontend/src/routes/Admin.svelte`
- Modify: `frontend/src/lib/api.ts` (review for admin API calls)

**Step 1: Update Admin.svelte API endpoints**

In `frontend/src/routes/Admin.svelte`, update the `loadData()` function and other API calls:

Change:
```typescript
  const [prodRes, catRes] = await Promise.all([
    api.get<ProductResponse[]>("/products"),
    api.get<Category[]>("/categories"),
  ]);
```
Remains the same (GET requests are still on public endpoints).

Change order loading from:
```typescript
  const res = await api.get<OrderResponse[]>("/orders");
```
to:
```typescript
  const res = await api.get<OrderResponse[]>("/admin/orders");
```

Change product delete from:
```typescript
  await api.delete(`/products/${id}`);
```
to:
```typescript
  await api.delete(`/admin/products/${id}`);
```

Change stock update from:
```typescript
  await api.patch(`/products/${id}/stock`, null, { params: { quantity } });
```
to:
```typescript
  await api.patch(`/admin/products/${id}/stock`, null, { params: { quantity } });
```

Change order status update from:
```typescript
  await api.patch(`/orders/${id}/status`, null, { params: { status } });
```
to:
```typescript
  await api.patch(`/admin/orders/${id}/status`, null, { params: { status } });
```

**Step 2: Update ProductFormModal.svelte API calls**

Find and update any admin API calls in `frontend/src/components/ProductFormModal.svelte`:
- POST/PUT to `/products` → `/admin/products`
- POST to `/products/{id}/datasheet` → `/admin/products/{id}/datasheet`

**Step 3: Verify frontend compiles**

Run: `cd /home/llmserver/Dev/SpecSheet Central/frontend && npx svelte-check --tsconfig ./tsconfig.json 2>&1 | tail -5`

Expected: No errors.

---

### Task 5: Update 401 interceptor redirect target

**Files:**
- Modify: `frontend/src/lib/api.ts`

**Step 1: Update the 401 interceptor**

In the response interceptor, change the redirect from `/` to `/login` for a better UX:

Change:
```typescript
  window.location.href = "/";
```
to:
```typescript
  window.location.href = "/login";
```

This way, when a 401 occurs, users are sent to the login page instead of the home page.

**Step 2: Verify frontend compiles**

Run: `cd /home/llmserver/Dev/SpecSheet Central/frontend && npx svelte-check --tsconfig ./tsconfig.json 2>&1 | tail -5`

Expected: No errors.

---

### Task 6: Remove onMount guard from Admin.svelte

This is already covered in Task 2, Step 4. Ensure that:
- `onMount` import is removed
- `navigate` import from `$lib/router` is removed
- The `onMount(() => { ... })` block is replaced with `$effect(() => { loadData(); })`

Verify that `auth` import is still needed (it is — AdminGuard uses it, but Admin.svelte still references `$auth` for displaying user info potentially, and needs the store for any role checks in the template).

Actually, reviewing the Admin.svelte code again — it only uses `$auth` in the `onMount` guard. After removing that, `$auth` is not referenced in the template. So we can also remove the `auth` import from Admin.svelte.

---

### Task 7: End-to-end verification

**Step 1: Build the backend**

Run: `cd /home/llmserver/Dev/SpecSheet Central/backend && ./mvnw package -DskipTests -q 2>&1 | tail -5`

Expected: BUILD SUCCESS

**Step 2: Build the frontend**

Run: `cd /home/llmserver/Dev/SpecSheet Central/frontend && npm run build 2>&1 | tail -10`

Expected: Build succeeds.

**Step 3: Commit all changes**

```bash
git add -A
git commit -m "feat: improve admin/user separation with route guards and /api/admin prefix"
```