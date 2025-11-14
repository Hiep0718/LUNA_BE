package iuh.fit.se.repositories;

import iuh.fit.se.entities.Brands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BrandRepository extends JpaRepository<Brands, Integer> {
    // Đếm số sản phẩm của 1 brand (để kiểm tra ràng buộc xóa)
    @Query("SELECT COUNT(p) FROM Products p WHERE p.brand.id = :brandId")
    long countProductsByBrandId(Integer brandId);
}