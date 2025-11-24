package iuh.fit.se.repositories;

import iuh.fit.se.entities.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Integer> {

    @Query("SELECT r FROM Reviews r " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH r.product " +
            "WHERE r.product.id = :productId AND r.product.isActive = true " +
            "ORDER BY r.createdAt DESC")
    List<Reviews> findByProductIdWithEagerLoad(@Param("productId") int productId);

    @Query("SELECT r FROM Reviews r " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH r.product " +
            "WHERE r.id = :id")
    Optional<Reviews> findByIdWithEagerLoad(@Param("id") int id);

    @Query("SELECT r FROM Reviews r " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH r.product " +
            "WHERE r.user.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<Reviews> findByUserIdWithEagerLoad(@Param("userId") Long userId);
}
