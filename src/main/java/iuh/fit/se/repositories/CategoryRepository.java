package iuh.fit.se.repositories;

import iuh.fit.se.entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {
    // Đếm số sản phẩm trong 1 category (để kiểm tra ràng buộc xóa)
    @Query("SELECT COUNT(p) FROM Products p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(Integer categoryId);
}
