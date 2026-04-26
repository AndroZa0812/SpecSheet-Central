# Datasheet Upload Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add the ability for admins to upload or fetch-by-URL PDF datasheets for each product, storing them locally on the server and serving them via `/uploads/`. Users always view datasheets from local storage, never external redirects.

**Architecture:** Extend `FileStorageService` with PDF-specific storage, fetching, and deletion. Add a `datasheetFilename` field to `Product`. Add a dedicated `POST /api/products/{id}/datasheet` multipart endpoint for datasheet management. Keep existing `POST/PUT /api/products` as JSON to minimize disruption. Frontend calls product save first, then datasheet endpoint if needed.

**Tech Stack:** Spring Boot 3, Java 21, Svelte 5, Axios, PostgreSQL, local filesystem storage

---

### Task 1: Extend FileStorageService with PDF operations

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/service/FileStorageService.java`
- Test: `backend/src/test/java/com/specsheetcentral/service/FileStorageServiceTest.java`

**Step 1: Write the failing test**

Add these tests to `FileStorageServiceTest.java`:

```java
@Test
void shouldStoreFileAndReturnFilenameOnly() {
    MockMultipartFile file = new MockMultipartFile(
        "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

    String filename = fileStorageService.storeFile(file);

    assertThat(filename).doesNotStartWith("/uploads/").endsWith(".pdf");
    assertThat(tempDir.resolve(filename)).exists();
}

@Test
void shouldDeleteFileByFilename() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());
    String filename = fileStorageService.storeFile(file);

    fileStorageService.deleteFile(filename);

    assertThat(tempDir.resolve(filename)).doesNotExist();
}

@Test
void shouldFetchPdfFromUrlAndStore() throws Exception {
    // Use a local file URL for testing
    Path fakePdf = tempDir.resolve("fake-remote.pdf");
    Files.writeString(fakePdf, "remote-pdf-content");
    String url = fakePdf.toUri().toURL().toString();

    String filename = fileStorageService.fetchAndStore(url);

    assertThat(filename).endsWith(".pdf");
    assertThat(tempDir.resolve(filename)).exists();
    assertThat(Files.readString(tempDir.resolve(filename))).isEqualTo("remote-pdf-content");
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=FileStorageServiceTest -v`
Expected: FAIL with "method storeFile not found"

**Step 3: Write minimal implementation**

Replace the contents of `FileStorageService.java` with:

```java
package com.specsheetcentral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        String filename = storeFile(file);
        return "/uploads/" + filename;
    }

    public String storeFile(MultipartFile file) {
        try {
            Path dir = getUploadDir();
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path dir = getUploadDir();
            Path target = dir.resolve(filename).normalize();
            if (!target.startsWith(dir)) {
                throw new SecurityException("Invalid filename");
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }
    }

    public String fetchAndStore(String url) {
        try {
            Path dir = getUploadDir();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + ".pdf";
            Path target = dir.resolve(filename);

            URL pdfUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pdfUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IllegalArgumentException("Failed to fetch PDF, HTTP " + responseCode);
            }
            String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().contains("pdf")) {
                throw new IllegalArgumentException("URL does not point to a PDF file (Content-Type: " + contentType + ")");
            }
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fetch PDF from URL", e);
        }
    }

    private Path getUploadDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int idx = originalFilename.lastIndexOf(".");
        return idx >= 0 ? originalFilename.substring(idx) : "";
    }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=FileStorageServiceTest -v`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/service/FileStorageService.java backend/src/test/java/com/specsheetcentral/service/FileStorageServiceTest.java
git commit -m "feat: extend FileStorageService with PDF store, fetch, and delete"
```

---

### Task 2: Update FileController to use storeFile helper

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/controller/FileController.java`
- Test: `backend/src/test/java/com/specsheetcentral/controller/FileControllerTest.java`

**Step 1: Modify FileController**

In `FileController.java`, change the upload method:

```java
@PostMapping("/upload")
public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
    String filename = fileStorageService.storeFile(file);
    return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
}
```

**Step 2: Update FileControllerTest**

The existing test should still pass since the response format is unchanged. Update the assertion to be more specific:

```java
@Test
void uploadShouldReturnUrl() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "content".getBytes());

    mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value(startsWith("/uploads/")));
}
```

Add the static import: `import static org.hamcrest.Matchers.startsWith;`

**Step 3: Run test**

Run: `cd backend && mvn test -Dtest=FileControllerTest -v`
Expected: PASS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/controller/FileController.java backend/src/test/java/com/specsheetcentral/controller/FileControllerTest.java
git commit -m "refactor: FileController uses storeFile helper"
```

---

### Task 3: Add datasheetFilename field to Product entity

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/model/Product.java`

**Step 1: Add field and accessors**

Add after the `datasheetUrl` field (around line 38):

```java
@Column
private String datasheetFilename;
```

Add getter/setter after the existing `datasheetUrl` getter/setter:

```java
public String getDatasheetFilename() { return datasheetFilename; }
public void setDatasheetFilename(String datasheetFilename) { this.datasheetFilename = datasheetFilename; }
```

**Step 2: No test needed for model change**

`ddl-auto=update` will create the column automatically on next app start.

**Step 3: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/model/Product.java
git commit -m "feat: add datasheetFilename field to Product entity"
```

---

### Task 4: Update ProductResponse DTO

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/dto/ProductResponse.java`

**Step 1: Add datasheetFilename field**

Add after the `datasheetUrl` field (around line 14):

```java
private String datasheetFilename;
```

Add getter/setter after the existing `datasheetUrl` getter/setter:

```java
public String getDatasheetFilename() { return datasheetFilename; }
public void setDatasheetFilename(String datasheetFilename) { this.datasheetFilename = datasheetFilename; }
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/dto/ProductResponse.java
git commit -m "feat: add datasheetFilename to ProductResponse"
```

---

### Task 5: Update ProductService with datasheet lifecycle

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/service/ProductService.java`
- Test: `backend/src/test/java/com/specsheetcentral/service/ProductServiceTest.java`

**Step 1: Write the failing test**

Add to `ProductServiceTest.java`. First, add the mock:

```java
@Mock
private FileStorageService fileStorageService;
```

Update `setUp()`:
```java
@BeforeEach
void setUp() {
    productService = new ProductService(productRepository, categoryRepository, productSpecRepository, fileStorageService);
    category = new Category();
    category.setId(1L);
    category.setName("Microcontrollers");
}
```

Add tests:

```java
@Test
void createWithDatasheetFileShouldStoreFile() {
    ProductRequest request = new ProductRequest();
    request.setName("Arduino Uno");
    request.setSku("ARD-UNO");
    request.setPrice(24.99);
    request.setStockQuantity(10);
    request.setCategoryId(1L);
    request.setDatasheetFile(new org.springframework.mock.web.MockMultipartFile(
        "file", "ds.pdf", "application/pdf", "pdf".getBytes()));

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
        Product p = invocation.getArgument(0);
        p.setId(1L);
        p.setCategory(category);
        return p;
    });
    when(fileStorageService.storeFile(any())).thenReturn("abc-123.pdf");

    ProductResponse result = productService.create(request);

    assertThat(result.getDatasheetFilename()).isEqualTo("abc-123.pdf");
    assertThat(result.getDatasheetUrl()).isEqualTo("/uploads/abc-123.pdf");
    verify(fileStorageService).storeFile(any());
}

@Test
void updateDatasheetShouldDeleteOldAndStoreNew() {
    Product product = new Product();
    product.setId(1L);
    product.setName("Arduino Uno");
    product.setSku("ARD-UNO");
    product.setPrice(24.99);
    product.setStockQuantity(10);
    product.setCategory(category);
    product.setDatasheetFilename("old-file.pdf");

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

    productService.updateDatasheet(1L,
        new org.springframework.mock.web.MockMultipartFile("file", "ds.pdf", "application/pdf", "pdf".getBytes()),
        null, false);

    verify(fileStorageService).deleteFile("old-file.pdf");
    verify(fileStorageService).storeFile(any());
}

@Test
void deleteShouldRemoveProductAndFile() {
    Product product = new Product();
    product.setId(1L);
    product.setDatasheetFilename("file.pdf");

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    doNothing().when(productRepository).deleteById(1L);

    productService.delete(1L);

    verify(fileStorageService).deleteFile("file.pdf");
    verify(productRepository).deleteById(1L);
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ProductServiceTest -v`
Expected: FAIL — constructor doesn't accept FileStorageService, method `updateDatasheet` not found

**Step 3: Write minimal implementation**

Replace `ProductService.java` with the updated version. Key changes:

1. Add `FileStorageService` field and constructor overloads:

```java
private final FileStorageService fileStorageService;

public ProductService(ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      ProductSpecRepository productSpecRepository,
                      FileStorageService fileStorageService) {
    this(productRepository, categoryRepository, productSpecRepository);
    this.fileStorageService = fileStorageService;
}

@Autowired
public ProductService(ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      ProductSpecRepository productSpecRepository,
                      ReviewRepository reviewRepository,
                      FileStorageService fileStorageService) {
    this(productRepository, categoryRepository, productSpecRepository, fileStorageService);
    this.reviewRepository = reviewRepository;
}
```

2. Update `create()` to handle `datasheetFile` and `datasheetUrl`:

After `product.setLowStockThreshold(...)`, add:

```java
if (request.getDatasheetFile() != null && !request.getDatasheetFile().isEmpty()) {
    String filename = fileStorageService.storeFile(request.getDatasheetFile());
    product.setDatasheetFilename(filename);
} else if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
    String filename = fileStorageService.fetchAndStore(request.getDatasheetUrl());
    product.setDatasheetFilename(filename);
}
```

3. Update `update()` similarly:

Before setting other fields, handle datasheet replacement:

```java
if (request.getDatasheetFile() != null && !request.getDatasheetFile().isEmpty()) {
    if (product.getDatasheetFilename() != null) {
        fileStorageService.deleteFile(product.getDatasheetFilename());
    }
    String filename = fileStorageService.storeFile(request.getDatasheetFile());
    product.setDatasheetFilename(filename);
} else if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
    if (product.getDatasheetFilename() != null) {
        fileStorageService.deleteFile(product.getDatasheetFilename());
    }
    String filename = fileStorageService.fetchAndStore(request.getDatasheetUrl());
    product.setDatasheetFilename(filename);
} else if (request.isClearDatasheet()) {
    if (product.getDatasheetFilename() != null) {
        fileStorageService.deleteFile(product.getDatasheetFilename());
    }
    product.setDatasheetFilename(null);
}
```

4. Update `delete()`:

```java
@Transactional
public void delete(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));
    if (product.getDatasheetFilename() != null) {
        fileStorageService.deleteFile(product.getDatasheetFilename());
    }
    productRepository.deleteById(id);
}
```

5. Add `updateDatasheet()` method:

```java
@Transactional
public ProductResponse updateDatasheet(Long id, MultipartFile file, String url, boolean clear) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));

    if (product.getDatasheetFilename() != null) {
        fileStorageService.deleteFile(product.getDatasheetFilename());
        product.setDatasheetFilename(null);
    }

    if (clear) {
        // already cleared above
    } else if (file != null && !file.isEmpty()) {
        String filename = fileStorageService.storeFile(file);
        product.setDatasheetFilename(filename);
    } else if (url != null && !url.isBlank()) {
        String filename = fileStorageService.fetchAndStore(url);
        product.setDatasheetFilename(filename);
    }

    return toResponse(productRepository.save(product));
}
```

6. Update `toResponse()`:

Replace the `datasheetUrl` line with:

```java
if (product.getDatasheetFilename() != null) {
    response.setDatasheetUrl("/uploads/" + product.getDatasheetFilename());
} else {
    response.setDatasheetUrl(product.getDatasheetUrl());
}
response.setDatasheetFilename(product.getDatasheetFilename());
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=ProductServiceTest -v`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/service/ProductService.java backend/src/test/java/com/specsheetcentral/service/ProductServiceTest.java
git commit -m "feat: add datasheet file lifecycle to ProductService"
```

---

### Task 6: Update ProductRequest DTO

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/dto/ProductRequest.java`

**Step 1: Add multipart and URL fields**

Add imports:
```java
import org.springframework.web.multipart.MultipartFile;
```

Add fields after `datasheetUrl`:
```java
private MultipartFile datasheetFile;
private boolean clearDatasheet;
```

Add getters/setters:
```java
public MultipartFile getDatasheetFile() { return datasheetFile; }
public void setDatasheetFile(MultipartFile datasheetFile) { this.datasheetFile = datasheetFile; }
public boolean isClearDatasheet() { return clearDatasheet; }
public void setClearDatasheet(boolean clearDatasheet) { this.clearDatasheet = clearDatasheet; }
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/dto/ProductRequest.java
git commit -m "feat: add datasheetFile and clearDatasheet to ProductRequest"
```

---

### Task 7: Add datasheet endpoint to ProductController

**Files:**
- Modify: `backend/src/main/java/com/specsheetcentral/controller/ProductController.java`
- Test: `backend/src/test/java/com/specsheetcentral/controller/ProductControllerTest.java`

**Step 1: Write the failing test**

Add to `ProductControllerTest.java`:

```java
@Test
void uploadDatasheetShouldReturnUpdatedProduct() throws Exception {
    ProductRequest request = new ProductRequest();
    request.setName("Arduino Uno");
    request.setSku("ARD-UNO-DS");
    request.setPrice(24.99);
    request.setStockQuantity(10);
    request.setCategoryId(category.getId());

    String json = mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    Long id = objectMapper.readTree(json).get("id").asLong();

    MockMultipartFile file = new MockMultipartFile(
            "datasheetFile", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

    mockMvc.perform(multipart("/api/products/{id}/datasheet", id)
                    .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datasheetFilename").exists())
            .andExpect(jsonPath("$.datasheetUrl").value(startsWith("/uploads/")));
}

@Test
void clearDatasheetShouldRemoveFilename() throws Exception {
    ProductRequest request = new ProductRequest();
    request.setName("Clear DS");
    request.setSku("CLR-001");
    request.setPrice(10.00);
    request.setStockQuantity(5);
    request.setCategoryId(category.getId());

    String json = mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    Long id = objectMapper.readTree(json).get("id").asLong();

    MockMultipartFile file = new MockMultipartFile(
            "datasheetFile", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());
    mockMvc.perform(multipart("/api/products/{id}/datasheet", id).file(file))
            .andExpect(status().isOk());

    mockMvc.perform(multipart("/api/products/{id}/datasheet", id)
                    .param("clearDatasheet", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datasheetFilename").isEmpty());
}
```

Add static import for `startsWith` if not already present.

**Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=ProductControllerTest#uploadDatasheetShouldReturnUpdatedProduct -v`
Expected: FAIL — endpoint `/api/products/{id}/datasheet` not found

**Step 3: Write minimal implementation**

Add to `ProductController.java`:

```java
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
```

Add the new endpoint:

```java
@PostMapping(value = "/{id}/datasheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ProductResponse uploadDatasheet(
        @PathVariable Long id,
        @RequestParam(value = "datasheetFile", required = false) MultipartFile datasheetFile,
        @RequestParam(value = "datasheetUrl", required = false) String datasheetUrl,
        @RequestParam(value = "clearDatasheet", required = false, defaultValue = "false") boolean clearDatasheet) {
    return productService.updateDatasheet(id, datasheetFile, datasheetUrl, clearDatasheet);
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=ProductControllerTest -v`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/specsheetcentral/controller/ProductController.java backend/src/test/java/com/specsheetcentral/controller/ProductControllerTest.java
git commit -m "feat: add POST /api/products/{id}/datasheet endpoint"
```

---

### Task 8: Update frontend types

**Files:**
- Modify: `frontend/src/lib/types.ts`

**Step 1: Add datasheetFilename to ProductResponse**

Update `ProductResponse` interface:

```typescript
export interface ProductResponse {
  id: number;
  name: string;
  sku: string;
  price: number;
  stockQuantity: number;
  lowStockThreshold: number;
  categoryName: string;
  categoryId?: number;
  manufacturer: string | null;
  imageUrl: string | null;
  datasheetUrl: string | null;
  datasheetFilename: string | null;
  specs: Record<string, string> | null;
  description: string | null;
  rating: number;
  reviewCount: number;
}
```

**Step 2: Commit**

```bash
git add frontend/src/lib/types.ts
git commit -m "feat: add datasheetFilename to frontend ProductResponse type"
```

---

### Task 9: Add datasheet API helper

**Files:**
- Modify: `frontend/src/lib/api.ts`

**Step 1: Add uploadDatasheet function**

Add after the existing exports:

```typescript
export async function uploadDatasheet(
  productId: number,
  file: File | null,
  url: string | null,
  clear: boolean,
) {
  const formData = new FormData();
  if (file) formData.append("datasheetFile", file);
  if (url) formData.append("datasheetUrl", url);
  if (clear) formData.append("clearDatasheet", "true");
  return api.post<ProductResponse>(`/products/${productId}/datasheet`, formData);
}
```

**Step 2: Commit**

```bash
git add frontend/src/lib/api.ts
git commit -m "feat: add uploadDatasheet API helper"
```

---

### Task 10: Update ProductFormModal for datasheet upload

**Files:**
- Modify: `frontend/src/components/ProductFormModal.svelte`

**Step 1: Update imports**

Change the api import to:
```typescript
import api, { uploadDatasheet } from "$lib/api";
```

**Step 2: Add datasheet state**

After the existing `form` state, add:

```typescript
let datasheetFile: File | null = $state(null);
let datasheetUrlInput = $state("");
let clearDatasheet = $state(false);
```

**Step 3: Update onMount to populate URL input from existing product**

In the `onMount` block, when `product` exists:

```typescript
datasheetUrlInput = product.datasheetUrl && product.datasheetFilename ? "" : (product.datasheetUrl || "");
```

**Step 4: Modify handleSubmit to use FormData and call uploadDatasheet**

Replace `handleSubmit` with:

```typescript
async function handleSubmit(e: Event) {
  e.preventDefault();
  saving = true;
  const data = {
    name: form.name,
    sku: form.sku,
    price: parseFloat(form.price),
    stockQuantity: parseInt(form.stockQuantity),
    lowStockThreshold: parseInt(form.lowStockThreshold),
    categoryId: parseInt(form.categoryId),
    manufacturer: form.manufacturer,
    imageUrl: form.imageUrl,
    specs: Object.fromEntries(
      form.specs.filter((s) => s.key).map((s) => [s.key, s.value]),
    ),
  };

  try {
    let savedProduct: ProductResponse;
    if (product) {
      const res = await api.put(`/products/${product.id}`, data);
      savedProduct = res.data;
    } else {
      const res = await api.post("/products", data);
      savedProduct = res.data;
    }

    if (datasheetFile || datasheetUrlInput || clearDatasheet) {
      await uploadDatasheet(
        savedProduct.id,
        datasheetFile,
        datasheetUrlInput || null,
        clearDatasheet,
      );
    }

    toast.success(product ? "Product updated" : "Product created");
    onsave();
  } catch (err) {
    toast.error((err as any).response?.data?.message || "Save failed");
  } finally {
    saving = false;
  }
}
```

**Step 5: Add UI controls for datasheet**

Replace the datasheet URL input section (around lines 172-175):

```svelte
<div class="flex flex-col gap-2">
  <Label for="datasheet">Datasheet</Label>
  {#if product?.datasheetFilename}
    <div class="flex items-center gap-2 text-sm">
      <span class="text-muted-foreground">Current: {product.datasheetFilename}</span>
      <Button type="button" variant="ghost" size="sm" onclick={() => { clearDatasheet = true; datasheetFile = null; datasheetUrlInput = ""; }}>
        <X class="mr-1 size-3.5" />
        Remove
      </Button>
    </div>
  {/if}
  {#if !clearDatasheet}
    <Input
      id="datasheet"
      type="file"
      accept=".pdf"
      onchange={(e) => { datasheetFile = (e.target as HTMLInputElement).files?.[0] ?? null; }}
    />
    <p class="text-xs text-muted-foreground">Or fetch from URL:</p>
    <Input
      placeholder="https://example.com/datasheet.pdf"
      bind:value={datasheetUrlInput}
    />
  {:else}
    <p class="text-sm text-muted-foreground">Datasheet will be removed on save.</p>
    <Button type="button" variant="ghost" size="sm" onclick={() => { clearDatasheet = false; }}>
      Keep existing
    </Button>
  {/if}
</div>
```

Add `FileUpload` or `Upload` icon to imports if desired (optional, not required).

**Step 6: Commit**

```bash
git add frontend/src/components/ProductFormModal.svelte
git commit -m "feat: add datasheet upload/fetch UI to ProductFormModal"
```

---

### Task 11: Verify ProductDetail works without changes

**Files:**
- No changes needed: `frontend/src/routes/ProductDetail.svelte`

The existing `ProductDetail.svelte` already links to `product.datasheetUrl`. Since `ProductService.toResponse()` now computes `datasheetUrl` as `/uploads/{filename}` for locally stored files, the button will correctly link to local files via the existing proxy.

**Step 1: Run backend tests**

Run: `cd backend && mvn test`
Expected: All 29+ tests PASS

**Step 2: Commit any remaining changes**

If tests required any fixes, commit them.

---

### Task 12: Manual end-to-end verification

**Step 1: Start the backend**

```bash
cd backend
mvn spring-boot:run
```

**Step 2: Start the frontend**

```bash
cd frontend
npm run dev
```

**Step 3: Log in as admin**
- Navigate to `http://localhost:5173/login`
- Use admin credentials

**Step 4: Test file upload**
- Go to Admin Panel → Add Product
- Fill in details, choose a PDF file for datasheet
- Save
- Verify the product detail page shows "View Datasheet" button
- Click it — PDF should open from `/uploads/{filename}`

**Step 5: Test URL fetch**
- Edit the product
- Clear the file, enter a URL to a PDF
- Save
- Verify the PDF is fetched and served locally

**Step 6: Test deletion**
- Delete the product
- Verify the PDF file is removed from `backend/uploads/` directory

**Step 7: Commit verification results**

```bash
git add -A
git commit -m "test: verify datasheet upload e2e"
```

---

## Summary of Changes

| File | Change |
|------|--------|
| `backend/src/main/java/.../service/FileStorageService.java` | Add `storeFile`, `deleteFile`, `fetchAndStore` |
| `backend/src/main/java/.../controller/FileController.java` | Use `storeFile` helper |
| `backend/src/main/java/.../model/Product.java` | Add `datasheetFilename` field |
| `backend/src/main/java/.../dto/ProductResponse.java` | Add `datasheetFilename` field |
| `backend/src/main/java/.../dto/ProductRequest.java` | Add `datasheetFile`, `clearDatasheet` |
| `backend/src/main/java/.../service/ProductService.java` | File lifecycle in create/update/delete, compute `datasheetUrl` in response |
| `backend/src/main/java/.../controller/ProductController.java` | Add `POST /{id}/datasheet` endpoint |
| `frontend/src/lib/types.ts` | Add `datasheetFilename` to `ProductResponse` |
| `frontend/src/lib/api.ts` | Add `uploadDatasheet` helper |
| `frontend/src/components/ProductFormModal.svelte` | File picker, URL input, remove button, FormData submission |
| Test files | Updated to cover new behavior |
