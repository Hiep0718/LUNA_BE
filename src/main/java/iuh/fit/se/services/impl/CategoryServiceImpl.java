package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.CategoryRequestDTO;
import iuh.fit.se.dtos.CategoryResponseDTO;
import iuh.fit.se.entities.Categories;
import iuh.fit.se.exceptions.ResourceNotFoundException;
import iuh.fit.se.repositories.CategoryRepository;
import iuh.fit.se.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO req) {
        Categories category = new Categories();
        category.setName(req.name());
        Categories savedCategory = categoryRepository.save(category);
        return mapToResponseDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponseDTO> getCategoryById(int id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    @Override
    public CategoryResponseDTO updateCategory(int id, CategoryRequestDTO req) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(req.name());
        Categories updatedCategory = categoryRepository.save(category);
        return mapToResponseDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(int id) {
        long productCount = countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalArgumentException("Cannot delete category. It contains " + productCount + " products.");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsByCategoryId(int categoryId) {
        return categoryRepository.countProductsByCategoryId(categoryId);
    }

    private CategoryResponseDTO mapToResponseDTO(Categories category) {
        return new CategoryResponseDTO(category.getId(), category.getName());
    }
}
