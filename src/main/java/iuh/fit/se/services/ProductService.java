package iuh.fit.se.services;

import iuh.fit.se.entities.Products;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Products> getAllProducts();
    Optional<Products> getProductById(int id);
}
