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
