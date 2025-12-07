package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.ReviewRequestDTO;
import iuh.fit.se.dtos.ReviewResponseDTO;
import iuh.fit.se.security.CustomUserDetails;
import iuh.fit.se.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id)
                .map(review -> ResponseEntity.ok(ApiResponse.success(200, "Review retrieved successfully", review)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Review not found"))
                );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByProduct(@PathVariable int productId) {
        try {
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByProductId(productId);
            return ResponseEntity.ok(ApiResponse.success(200, "Reviews retrieved successfully", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByUser(@PathVariable Long userId) {
        try {
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(200, "Reviews retrieved successfully", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview(
            @RequestBody ReviewRequestDTO reviewRequestDTO,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = jwt.getClaim("id");

            ReviewResponseDTO createdReview =
                    reviewService.createReview(reviewRequestDTO, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Review created successfully", createdReview));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Internal Server Error", e.getMessage()));
        }
    }

}
