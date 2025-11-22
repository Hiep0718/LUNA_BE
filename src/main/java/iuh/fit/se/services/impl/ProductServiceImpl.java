package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.ProductResponseDTO;
import iuh.fit.se.entities.Products;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAllActive()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductResponseDTO> getProductById(int id) {
        return productRepository.findActiveById(id)
                .map(this::convertToDTO);
    }

    private ProductResponseDTO convertToDTO(Products product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.isActive())
                .brand(product.getBrand() != null ? ProductResponseDTO.BrandDTO.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build() : null)
                .category(product.getCategory() != null ? ProductResponseDTO.CategoryDTO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build() : null)
                .productImages(product.getProductImages() != null ?
                        product.getProductImages().stream()
                                .map(img -> ProductResponseDTO.ProductImageDTO.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .isDefault(img.isDefault())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .productAttributes(product.getProductAttributes() != null ?
                        product.getProductAttributes().stream()
                                .map(attr -> ProductResponseDTO.ProductAttributeDTO.builder()
                                        .id(attr.getAttribute().getId())
                                        .attributeName(attr.getAttribute().getName())
                                        .attributeValue(attr.getValue())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .reviews(product.getReviews() != null ?
                        product.getReviews().stream()
                                .map(review -> ProductResponseDTO.ReviewDTO.builder()
                                        .id(review.getId())
                                        .reviewText(review.getComment())
                                        .rating(review.getRating())
                                        .userName(review.getUser() != null ? review.getUser().getFullName(): "Anonymous")
                                        .build())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
