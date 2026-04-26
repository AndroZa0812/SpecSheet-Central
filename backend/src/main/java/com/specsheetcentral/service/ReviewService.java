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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String REVIEW_NOT_FOUND = "Review not found";
    private static final String ALREADY_REVIEWED = "User has already reviewed this product";
    private static final String UNAUTHORIZED_DELETE = "You can only delete your own reviews";
    private static final String UNAUTHORIZED_UPDATE = "You can only update your own reviews";

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional
    public ReviewResponse createReview(Long productId, Long userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        if (reviewRepository.findByProductIdAndUserId(productId, userId).isPresent()) {
            throw new IllegalStateException(ALREADY_REVIEWED);
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        productService.recalculateProductRating(productId);

        return toResponse(saved);
    }

    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException(UNAUTHORIZED_UPDATE);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        productService.recalculateProductRating(review.getProduct().getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(REVIEW_NOT_FOUND));

        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new SecurityException(UNAUTHORIZED_DELETE);
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        productService.recalculateProductRating(productId);
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setUserEmail(review.getUser().getEmail());
        response.setCreatedAt(review.getCreatedAt());
        response.setProductId(review.getProduct().getId());
        return response;
    }
}
