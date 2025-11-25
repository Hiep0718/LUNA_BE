package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.ProductImageListDTO;
import iuh.fit.se.dtos.ProductRequestDTO;
import iuh.fit.se.dtos.ProductImageRequestDTO;
import iuh.fit.se.dtos.ProductResponseDTO;
import iuh.fit.se.entities.Brands;
import iuh.fit.se.entities.Categories;
import iuh.fit.se.entities.Products;
import iuh.fit.se.entities.ProductImages;
import iuh.fit.se.repositories.BrandRepository;
import iuh.fit.se.repositories.CategoryRepository;
import iuh.fit.se.repositories.OrderDetailsRepository;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.repositories.ProductImageRepository;
import iuh.fit.se.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageService imageService;

    @Autowired
    public AdminProductController(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, OrderDetailsRepository orderDetailsRepository, ProductImageRepository productImageRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productImageRepository = productImageRepository;
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts() {
        var products = productRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(200, "Products retrieved successfully", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product ID must be positive"));
        }

        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(200, "Product retrieved successfully", convertToDTO(product))))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Product not found"))
                );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(@RequestBody ProductRequestDTO req) {
        if (req == null || req.name() == null || req.name().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product name is required"));
        }

        if (req.price() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product price must be positive"));
        }

        if (req.stockQuantity() < 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Stock quantity cannot be negative"));
        }

        Categories cat = categoryRepository.findById(req.categoryId()).orElse(null);
        Brands brand = brandRepository.findById(req.brandId()).orElse(null);

        if (cat == null || brand == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Category or Brand not found"));
        }

        Products p = new Products();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setActive(req.isActive());
        p.setCategory(cat);
        p.setBrand(brand);
        p.setCreatedBy(1L);

        Products savedProduct = productRepository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Product created successfully", convertToDTO(savedProduct)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(@PathVariable int id, @RequestBody ProductRequestDTO req) {
        if (id <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product ID must be positive"));
        }

        if (req == null || req.name() == null || req.name().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product name is required"));
        }

        if (req.price() <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product price must be positive"));
        }

        var existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Product not found"));
        }

        Products p = existingProduct.get();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setActive(req.isActive());
        p.setUpdatedBy(1L);

        Products savedProduct = productRepository.save(p);
        return ResponseEntity.ok(ApiResponse.success(200, "Product updated successfully", convertToDTO(savedProduct)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product ID must be positive"));
        }

        Optional<Products> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Product not found"));
        }

        long orderCount = orderDetailsRepository.countByProductId(id);
        if (orderCount > 0) {
            String errorMsg = "Cannot delete product. It exists in " + orderCount + " orders.";
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", errorMsg));
        }

        productRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Product deleted successfully", null));
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<?>> uploadProductImage(
            @PathVariable int productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDefault", defaultValue = "false") boolean isDefault) {

        if (productId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product ID must be positive"));
        }

        Optional<Products> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Product not found"));
        }

        try {
            String imagePath = imageService.saveImage(file);

            if (isDefault) {
                productImageRepository.findByProductIdAndIsDefault(productId, true)
                        .ifPresent(img -> {
                            img.setDefault(false);
                            productImageRepository.save(img);
                        });
            }

            ProductImages productImage = new ProductImages();
            productImage.setProduct(productOpt.get());
            productImage.setImageUrl(imagePath);
            productImage.setDefault(isDefault);

            ProductImages savedImage = productImageRepository.save(productImage);

            ProductImageListDTO imageDTO = ProductImageListDTO.builder()
                    .id(savedImage.getId())
                    .imageUrl(savedImage.getImageUrl())
                    .isDefault(savedImage.isDefault())
                    .createdAt(savedImage.getCreatedAt() != null ? savedImage.getCreatedAt().toString() : null)
                    .updatedAt(savedImage.getUpdatedAt() != null ? savedImage.getUpdatedAt().toString() : null)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Image uploaded successfully", imageDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Internal Server Error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteProductImage(@PathVariable int imageId) {
        if (imageId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Image ID must be positive"));
        }

        Optional<ProductImages> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Image not found"));
        }

        try {
            ProductImages image = imageOpt.get();
            imageService.deleteImage(image.getImageUrl());
            productImageRepository.deleteById(imageId);

            ProductImageListDTO imageDTO = ProductImageListDTO.builder()
                    .id(image.getId())
                    .imageUrl(image.getImageUrl())
                    .isDefault(image.isDefault())
                    .createdAt(image.getCreatedAt() != null ? image.getCreatedAt().toString() : null)
                    .updatedAt(image.getUpdatedAt() != null ? image.getUpdatedAt().toString() : null)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(200, "Image deleted successfully", imageDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Internal Server Error", "Failed to delete image: " + e.getMessage()));
        }
    }

    @PutMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> updateProductImage(
            @PathVariable int imageId,
            @RequestBody ProductImageRequestDTO req) {

        if (imageId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Image ID must be positive"));
        }

        Optional<ProductImages> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Image not found"));
        }

        try {
            ProductImages image = imageOpt.get();

            if (req.isDefault()) {
                productImageRepository.findByProductIdAndIsDefault(image.getProduct().getId(), true)
                        .ifPresent(img -> {
                            if (img.getId() != imageId) {
                                img.setDefault(false);
                                productImageRepository.save(img);
                            }
                        });
            }

            image.setDefault(req.isDefault());
            ProductImages updatedImage = productImageRepository.save(image);

            ProductImageListDTO imageDTO = ProductImageListDTO.builder()
                    .id(updatedImage.getId())
                    .imageUrl(updatedImage.getImageUrl())
                    .isDefault(updatedImage.isDefault())
                    .createdAt(updatedImage.getCreatedAt() != null ? updatedImage.getCreatedAt().toString() : null)
                    .updatedAt(updatedImage.getUpdatedAt() != null ? updatedImage.getUpdatedAt().toString() : null)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(200, "Image updated successfully", imageDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Internal Server Error", "Failed to update image: " + e.getMessage()));
        }
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<?>> getProductImages(@PathVariable int productId) {
        if (productId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Product ID must be positive"));
        }

        Optional<Products> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Product not found"));
        }

        List<ProductImages> images = productImageRepository.findByProductIdOrderByDefault(productId);
        List<ProductImageListDTO> imageDTOs = images.stream()
                .map(img -> ProductImageListDTO.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isDefault(img.isDefault())
                        .createdAt(img.getCreatedAt() != null ? img.getCreatedAt().toString() : null)
                        .updatedAt(img.getUpdatedAt() != null ? img.getUpdatedAt().toString() : null)
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(200, "Product images retrieved successfully", imageDTOs));
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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
