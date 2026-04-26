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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReviewService reviewService;

    private Product product;
    private User user;
    private Review review;
    private ReviewRequest request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Widget");

        user = new User();
        user.setId(2L);
        user.setEmail("test@example.com");

        request = new ReviewRequest();
        request.setRating(4);
        request.setComment("Great product");

        review = new Review();
        review.setId(10L);
        review.setProduct(product);
        review.setUser(user);
        review.setRating(4);
        review.setComment("Great product");
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReview_validRequest_returnsResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        ReviewResponse response = reviewService.createReview(1L, 2L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(4, response.getRating());
        assertEquals("Great product", response.getComment());
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals(1L, response.getProductId());
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void createReview_duplicateReview_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductIdAndUserId(1L, 2L)).thenReturn(Optional.of(review));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reviewService.createReview(1L, 2L, request));
        assertEquals("User has already reviewed this product", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_nonexistentProduct_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> reviewService.createReview(999L, 2L, request));
        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void getReviewsByProduct_returnsList() {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findByProductId(1L)).thenReturn(reviews);

        List<ReviewResponse> responses = reviewService.getReviewsByProduct(1L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
        assertEquals(4, responses.get(0).getRating());
        assertEquals("test@example.com", responses.get(0).getUserEmail());
    }

    @Test
    void deleteReview_ownReview_succeeds() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(10L, 2L, false);

        verify(reviewRepository).delete(review);
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void deleteReview_adminDeleteOtherUsersReview_succeeds() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(10L, 99L, true);

        verify(reviewRepository).delete(review);
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void deleteReview_nonOwner_throwsSecurityException() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> reviewService.deleteReview(10L, 99L, false));
        assertEquals("You can only delete your own reviews", ex.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void updateReview_byOwner_succeeds() {
        ReviewRequest updateRequest = new ReviewRequest();
        updateRequest.setRating(5);
        updateRequest.setComment("Updated comment");

        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.updateReview(10L, 2L, updateRequest);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Updated comment", response.getComment());
        verify(productService).recalculateProductRating(1L);
    }

    @Test
    void updateReview_byNonOwner_throwsSecurityException() {
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> reviewService.updateReview(10L, 99L, request));
        assertEquals("You can only update your own reviews", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }
}