package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.ReviewRequestDTO;
import iuh.fit.se.dtos.ReviewResponseDTO;
import iuh.fit.se.entities.Products;
import iuh.fit.se.entities.Reviews;
import iuh.fit.se.entities.User;
import iuh.fit.se.exceptions.ResourceNotFoundException;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.repositories.ReviewRepository;
import iuh.fit.se.repositories.UserRepository;
import iuh.fit.se.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductRepository productRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO, Long userId) {
        Products product = productRepository.findById(reviewRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product is not active");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (reviewRequestDTO.getRating() < 1 || reviewRequestDTO.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Reviews review = new Reviews();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(reviewRequestDTO.getRating());
        review.setComment(reviewRequestDTO.getComment());
        review.setCreatedBy(userId);
        review.setUpdatedBy(userId);

        Reviews savedReview = reviewRepository.save(review);

        return reviewRepository.findByIdWithEagerLoad(savedReview.getId())
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created review"));
    }

    @Override
    public Optional<ReviewResponseDTO> getReviewById(int id) {
        return reviewRepository.findByIdWithEagerLoad(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByProductId(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }

        return reviewRepository.findByProductIdWithEagerLoad(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return reviewRepository.findByUserIdWithEagerLoad(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO convertToDTO(Reviews review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
