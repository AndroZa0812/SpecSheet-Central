package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.service.FileStorageService;
import com.specsheetcentral.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final FileStorageService fileStorageService;

    public ProductController(ProductService productService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
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

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request) {
        if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
            request.setDatasheetUrl(fileStorageService.sanitizeUrl(request.getDatasheetUrl()));
        }
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request) {
        if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
            request.setDatasheetUrl(fileStorageService.sanitizeUrl(request.getDatasheetUrl()));
        }
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

    @PostMapping(value = "/{id}/datasheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadDatasheet(
            @PathVariable Long id,
            @RequestParam(value = "datasheetFile", required = false) MultipartFile datasheetFile,
            @RequestParam(value = "datasheetUrl", required = false) String datasheetUrl,
            @RequestParam(value = "clearDatasheet", required = false, defaultValue = "false") boolean clearDatasheet) {
        final String sanitizedUrl = (datasheetUrl != null && !datasheetUrl.isBlank())
                ? fileStorageService.sanitizeUrl(datasheetUrl) : datasheetUrl;
        return productService.updateDatasheet(id, datasheetFile, sanitizedUrl, clearDatasheet);
    }
}