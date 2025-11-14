package iuh.fit.se.controllers;

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
    public ResponseEntity<List<Products>> getAllProducts() {
        // Nên dùng Phân trang (Pageable) ở đây
        return ResponseEntity.ok(productRepository.findAll());
    }

    // Yêu cầu: Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable int id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Yêu cầu: Thêm mới
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDTO req) {
        Categories cat = categoryRepository.findById(req.categoryId())
                .orElse(null);
        Brands brand = brandRepository.findById(req.brandId())
                .orElse(null);

        if (cat == null || brand == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category or Brand not found"));
        }

        Products p = new Products();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setActive(req.isActive());
        p.setCategory(cat);
        p.setBrand(brand);
        // (Set createdBy/updatedBy nếu cần)

        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(p));
    }

    // Yêu cầu: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody ProductRequestDTO req) {
        Optional<Products> existingProductOpt = productRepository.findById(id);
        if (existingProductOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // (Tương tự logic create, tìm Category/Brand...)

        Products p = existingProductOpt.get();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setActive(req.isActive());
        // (Cập nhật Category/Brand nếu ID thay đổi)

        return ResponseEntity.ok(productRepository.save(p));
    }

    // Yêu cầu: Ràng buộc khi xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        long orderCount = orderDetailsRepository.countByProductId(id);
        if (orderCount > 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete product. It exists in " + orderCount + " orders."));
        }

        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }
}