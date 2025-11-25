package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private boolean isActive;

    // Brand info
    private BrandDTO brand;

    // Category info
    private CategoryDTO category;

    // Product images
    private List<ProductImageDTO> productImages;

    // Product attributes
    private List<ProductAttributeDTO> productAttributes;

    // Reviews
    private List<ReviewDTO> reviews;

    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrandDTO {
        private int id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDTO {
        private int id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImageDTO {
        private int id;
        private String imageUrl;
        private boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductAttributeDTO {
        private int id;
        private String attributeName;
        private String attributeValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDTO {
        private int id;
        private String reviewText;
        private int rating;
        private String userName;
    }
}
