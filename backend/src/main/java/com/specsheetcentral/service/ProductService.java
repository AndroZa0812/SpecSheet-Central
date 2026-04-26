package com.specsheetcentral.service;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.ProductSpec;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import com.specsheetcentral.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String PRODUCT_NOT_FOUND = "Product not found";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecRepository productSpecRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductSpecRepository productSpecRepository,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSpecRepository = productSpecRepository;
        this.fileStorageService = fileStorageService;
        this.reviewRepository = null;
    }

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductSpecRepository productSpecRepository,
                          ReviewRepository reviewRepository,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSpecRepository = productSpecRepository;
        this.reviewRepository = reviewRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ProductResponse> findAll(String search, List<Long> categoryIds, String manufacturer, Double minPrice, Double maxPrice) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
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
            .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setManufacturer(request.getManufacturer());
        product.setImageUrl(request.getImageUrl());
        product.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);

        if (request.getDatasheetFile() != null && !request.getDatasheetFile().isEmpty()) {
            String filename = fileStorageService.storeFile(request.getDatasheetFile());
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        } else if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
            String filename = fileStorageService.fetchAndStore(request.getDatasheetUrl());
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        } else {
            product.setDatasheetUrl(request.getDatasheetUrl());
        }

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
                .toList();
            productSpecRepository.saveAll(specs);
            saved.setSpecs(specs);
        }

        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setManufacturer(request.getManufacturer());
        product.setImageUrl(request.getImageUrl());
        product.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);

        if (request.getDatasheetFile() != null && !request.getDatasheetFile().isEmpty()) {
            if (product.getDatasheetFilename() != null) {
                fileStorageService.deleteFile(product.getDatasheetFilename());
            }
            String filename = fileStorageService.storeFile(request.getDatasheetFile());
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        } else if (request.getDatasheetUrl() != null && !request.getDatasheetUrl().isBlank()) {
            if (product.getDatasheetFilename() != null) {
                fileStorageService.deleteFile(product.getDatasheetFilename());
            }
            String filename = fileStorageService.fetchAndStore(request.getDatasheetUrl());
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        } else if (request.isClearDatasheet()) {
            if (product.getDatasheetFilename() != null) {
                fileStorageService.deleteFile(product.getDatasheetFilename());
            }
            product.setDatasheetFilename(null);
            product.setDatasheetUrl(null);
        } else {
            product.setDatasheetUrl(request.getDatasheetUrl());
        }

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
                .toList();
            productSpecRepository.saveAll(specs);
            product.setSpecs(specs);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));
        if (product.getDatasheetFilename() != null) {
            fileStorageService.deleteFile(product.getDatasheetFilename());
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse updateDatasheet(Long id, MultipartFile file, String url, boolean clear) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));

        if (product.getDatasheetFilename() != null) {
            fileStorageService.deleteFile(product.getDatasheetFilename());
            product.setDatasheetFilename(null);
            product.setDatasheetUrl(null);
        }

        if (clear) {
            product.setDatasheetFilename(null);
            product.setDatasheetUrl(null);
        } else if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.storeFile(file);
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        } else if (url != null && !url.isBlank()) {
            String filename = fileStorageService.fetchAndStore(url);
            product.setDatasheetFilename(filename);
            product.setDatasheetUrl("/uploads/" + filename);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));
        product.setStockQuantity(quantity);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void recalculateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));

        Double avg = reviewRepository.averageRatingByProductId(productId);
        product.setRating(avg);
        product.setReviewCount(reviewRepository.countByProductId(productId));
        productRepository.save(product);
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
        if (product.getDatasheetFilename() != null) {
            response.setDatasheetUrl("/uploads/" + product.getDatasheetFilename());
        } else {
            response.setDatasheetUrl(product.getDatasheetUrl());
        }
        response.setDatasheetFilename(product.getDatasheetFilename());
        if (product.getSpecs() != null) {
            Map<String, String> specs = product.getSpecs().stream()
                .collect(Collectors.toMap(ProductSpec::getSpecKey, ProductSpec::getSpecValue));
            response.setSpecs(specs);
        }
        response.setRating(product.getRating());
        response.setReviewCount(product.getReviewCount());
        response.setLowStockThreshold(product.getLowStockThreshold());
        return response;
    }
}
