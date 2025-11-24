package iuh.fit.se.services;

import iuh.fit.se.dtos.ReviewRequestDTO;
import iuh.fit.se.dtos.ReviewResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO, Long userId);
    Optional<ReviewResponseDTO> getReviewById(int id);
    List<ReviewResponseDTO> getReviewsByProductId(int productId);
    List<ReviewResponseDTO> getReviewsByUserId(Long userId);
}
