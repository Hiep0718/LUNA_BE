package iuh.fit.se.repositories;

import iuh.fit.se.entities.ProductImages;
import iuh.fit.se.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImages, Integer> {

    List<ProductImages> findByProduct(Products product);

    @Query("SELECT pi FROM ProductImages pi WHERE pi.product.id = :productId ORDER BY pi.isDefault DESC")
    List<ProductImages> findByProductIdOrderByDefault(@Param("productId") int productId);

    Optional<ProductImages> findByProductIdAndIsDefault(@Param("productId") int productId, @Param("isDefault") boolean isDefault);

    void deleteByProduct(Products product);
}
