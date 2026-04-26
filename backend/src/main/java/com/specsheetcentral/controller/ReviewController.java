package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ReviewRequest;
import com.specsheetcentral.dto.ReviewResponse;
import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import com.specsheetcentral.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private static final String USER_NOT_FOUND = "User not found";

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @PostMapping("/products/{productId}/reviews")
    public ReviewResponse create(@PathVariable Long productId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @RequestBody ReviewRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        return reviewService.createReview(productId, user.getId(), request);
    }

    @GetMapping("/products/{productId}/reviews")
    public List<ReviewResponse> getByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse update(@PathVariable Long reviewId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @RequestBody ReviewRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        return reviewService.updateReview(reviewId, user.getId(), request);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void delete(@PathVariable Long reviewId,
                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(reviewId, user.getId(), isAdmin);
    }
}
