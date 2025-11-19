package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CategoryRequestDTO;
import iuh.fit.se.dtos.CategoryResponseDTO;
import iuh.fit.se.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCategory(@RequestBody CategoryRequestDTO req) {
        CategoryResponseDTO category = categoryService.createCategory(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Category created successfully", category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(200, "Categories retrieved successfully", categories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(@PathVariable int id, @RequestBody CategoryRequestDTO req) {
        CategoryResponseDTO category = categoryService.updateCategory(id, req);
        return ResponseEntity.ok(ApiResponse.success(200, "Category updated successfully", category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable int id) {
        return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(ApiResponse.success(200, "Category retrieved successfully", category)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Category not found"))
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable int id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success(200, "Category deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Category not found"));
        }
    }
}
