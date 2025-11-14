package iuh.fit.se.services.impl;

import iuh.fit.se.entities.Products;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Products> getAllProducts() {
        // Chỉ lấy các sản phẩm đang active
        return productRepository.findAll().stream()
                .filter(Products::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Products> getProductById(int id) {
        return productRepository.findById(id)
                .filter(Products::isActive); // Chỉ trả về nếu active
    }
}
