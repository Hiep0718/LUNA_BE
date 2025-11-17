package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.ProductRequestDTO;
import iuh.fit.se.entities.Brands;
import iuh.fit.se.entities.Categories;
import iuh.fit.se.entities.Products;
import iuh.fit.se.repositories.BrandRepository;
import iuh.fit.se.repositories.CategoryRepository;
import iuh.fit.se.repositories.OrderDetailsRepository;
import iuh.fit.se.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// AdminProductController.java (File mới)
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderDetailsRepository orderDetailsRepository; // Để check ràng buộc xóa

    @Autowired
    public AdminProductController(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, OrderDetailsRepository orderDetailsRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    // Yêu cầu: Tìm kiếm/Xem danh sách sản phẩm
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(200, "Products retrieved successfully", productRepository.findAll()));
    }

    // Yêu cầu: Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Products>> getProductById(@PathVariable int id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(200, "Product retrieved successfully", product)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Product not found"))
                );
    }

    // Yêu cầu: Thêm mới
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(@RequestBody ProductRequestDTO req) {
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Product created successfully", productRepository.save(p)));
    }

    // Yêu cầu: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(@PathVariable int id, @RequestBody ProductRequestDTO req) {
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

        return ResponseEntity.ok(ApiResponse.success(200, "Product updated successfully", productRepository.save(p)));
    }

    // Yêu cầu: Ràng buộc khi xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable int id) {
        long orderCount = orderDetailsRepository.countByProductId(id);
        if (orderCount > 0) {
            String errorMsg = "Cannot delete product. It exists in " + orderCount + " orders.";
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", errorMsg));
        }

        productRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Product deleted successfully", null));
    }
}
