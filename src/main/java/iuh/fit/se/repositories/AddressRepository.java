package iuh.fit.se.repositories;

import iuh.fit.se.entities.Addresses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Addresses, Integer> {
}