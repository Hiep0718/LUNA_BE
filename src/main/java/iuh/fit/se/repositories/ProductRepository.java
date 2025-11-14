package iuh.fit.se.repositories;

import iuh.fit.se.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, Integer> {

}
