package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.BrandRequestDTO;
import iuh.fit.se.dtos.BrandResponseDTO;
import iuh.fit.se.entities.Brands;
import iuh.fit.se.exceptions.ResourceNotFoundException;
import iuh.fit.se.repositories.BrandRepository;
import iuh.fit.se.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public BrandResponseDTO createBrand(BrandRequestDTO req) {
        Brands brand = new Brands();
        brand.setName(req.name());
        Brands savedBrand = brandRepository.save(brand);
        return mapToResponseDTO(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrandResponseDTO> getBrandById(int id) {
        return brandRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    @Override
    public BrandResponseDTO updateBrand(int id, BrandRequestDTO req) {
        Brands brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        brand.setName(req.name());
        Brands updatedBrand = brandRepository.save(brand);
        return mapToResponseDTO(updatedBrand);
    }

    @Override
    public void deleteBrand(int id) {
        long productCount = countProductsByBrandId(id);
        if (productCount > 0) {
            throw new IllegalArgumentException("Cannot delete brand. It contains " + productCount + " products.");
        }
        brandRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsByBrandId(int brandId) {
        return brandRepository.countProductsByBrandId(brandId);
    }

    private BrandResponseDTO mapToResponseDTO(Brands brand) {
        return new BrandResponseDTO(brand.getId(), brand.getName());
    }
}
