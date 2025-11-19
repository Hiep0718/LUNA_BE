package iuh.fit.se.services;

import iuh.fit.se.dtos.CategoryRequestDTO;
import iuh.fit.se.dtos.CategoryResponseDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO req);
    List<CategoryResponseDTO> getAllCategories();
    Optional<CategoryResponseDTO> getCategoryById(int id);
    CategoryResponseDTO updateCategory(int id, CategoryRequestDTO req);
    void deleteCategory(int id);
    long countProductsByCategoryId(int categoryId);
}
