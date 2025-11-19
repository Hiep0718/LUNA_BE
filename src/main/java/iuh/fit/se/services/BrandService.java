package iuh.fit.se.services;

import iuh.fit.se.dtos.BrandRequestDTO;
import iuh.fit.se.dtos.BrandResponseDTO;

import java.util.List;
import java.util.Optional;

public interface BrandService {
    BrandResponseDTO createBrand(BrandRequestDTO req);
    List<BrandResponseDTO> getAllBrands();
    Optional<BrandResponseDTO> getBrandById(int id);
    BrandResponseDTO updateBrand(int id, BrandRequestDTO req);
    void deleteBrand(int id);
    long countProductsByBrandId(int brandId);
}
