package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CategoryResponseDTO;
import iuh.fit.se.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories") // Đường dẫn chung, không có chữ 'admin'
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // API lấy tất cả danh sách (Public)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(200, "Categories retrieved successfully", categories));
    }

    // API lấy chi tiết 1 category (Public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable int id) {
        return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(ApiResponse.success(200, "Category retrieved successfully", category)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}