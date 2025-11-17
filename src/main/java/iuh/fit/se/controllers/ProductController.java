package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.entities.Products;
import iuh.fit.se.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Yêu cầu: Xem danh sách sản phẩm (Guest)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllProducts() {
        // Note: Nên dùng DTO và Phân trang (Pagination) ở đây
        // Nhưng để đơn giản, chúng ta trả về List
        return ResponseEntity.ok(ApiResponse.success(200, "Products retrieved successfully", productService.getAllProducts()));
    }

    // Yêu cầu: Xem chi tiết sản phẩm (Guest)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Products>> getProductById(@PathVariable int id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(200, "Product retrieved successfully", product)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Product not found or not active"))
                );
    }
}
