package iuh.fit.se.repositories;

import iuh.fit.se.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Integer> {

    @Query("SELECT DISTINCT p FROM Products p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.isActive = true")
    List<Products> findAllActive();

    @Query("SELECT p FROM Products p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.id = :id AND p.isActive = true")
    Optional<Products> findActiveById(@Param("id") int id);
}
