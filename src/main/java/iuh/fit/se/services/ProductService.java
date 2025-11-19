package iuh.fit.se.services;

import iuh.fit.se.dtos.ProductResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductResponseDTO> getAllProducts();
    Optional<ProductResponseDTO> getProductById(int id);
}
