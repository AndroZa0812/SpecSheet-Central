package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
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