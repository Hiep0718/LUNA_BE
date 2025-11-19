package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CategoryRequestDTO;
import iuh.fit.se.entities.Categories;
import iuh.fit.se.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ ở cấp độ controller
public class AdminCategoryController {

    private final CategoryRepository categoryRepository; // Tạm dùng Repo trực tiếp cho gọn

    @Autowired
    public AdminCategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCategory(@RequestBody CategoryRequestDTO req) {
        Categories cat = new Categories();
        cat.setName(req.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Category created successfully", categoryRepository.save(cat)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(200, "Categories retrieved successfully", categoryRepository.findAll()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Categories>> updateCategory(@PathVariable int id, @RequestBody CategoryRequestDTO req) {
        return categoryRepository.findById(id).map(cat -> {
            cat.setName(req.name());
            categoryRepository.save(cat);
            return ResponseEntity.ok(ApiResponse.success(200, "Category updated successfully", cat));
        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Not Found", "Category not found"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Categories>> getCategoryById(@PathVariable int id) {
        return categoryRepository.findById(id)
                .map(cat -> ResponseEntity.ok(ApiResponse.success(200, "Category retrieved successfully", cat)))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Not Found", "Category not found"))
                );
    }

    // Yêu cầu: Ràng buộc khi xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable int id) {
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Cannot delete category. It contains " + productCount + " products."));
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Category deleted successfully", null));
    }
}
