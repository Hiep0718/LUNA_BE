package iuh.fit.se.repositories;

import iuh.fit.se.entities.Discounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountsRepository extends JpaRepository<Discounts, Integer> {
}
