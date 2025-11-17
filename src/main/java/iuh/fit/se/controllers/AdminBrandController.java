package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.BrandRequestDTO;
import iuh.fit.se.entities.Brands;
import iuh.fit.se.repositories.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/brands")
@PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN được phép truy cập
public class AdminBrandController {

    private final BrandRepository brandRepository;

    @Autowired
    public AdminBrandController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBrand(@RequestBody BrandRequestDTO req) {
        Brands brand = new Brands();
        brand.setName(req.name());
        Brands savedBrand = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Brand created successfully", savedBrand));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands() {
        return ResponseEntity.ok(ApiResponse.success(200, "Brands retrieved successfully", brandRepository.findAll()));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Brands>> updateBrand(@PathVariable int id, @RequestBody BrandRequestDTO req) {
        return brandRepository.findById(id).map(brand -> {
            brand.setName(req.name());
            brandRepository.save(brand);
            return ResponseEntity.ok(ApiResponse.success(200, "Brand updated successfully", brand));
        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Not Found", "Brand not found"))
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Brands>> getBrandById(@PathVariable int id) {
        return brandRepository.findById(id)
                .map(brand -> ResponseEntity.ok(ApiResponse.success(200, "Brand retrieved successfully", brand)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Brand not found"))
                );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBrand(@PathVariable int id) {
        long productCount = brandRepository.countProductsByBrandId(id);

        if (productCount > 0) {
            String errorMsg = "Cannot delete brand. It contains " + productCount + " products.";
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", errorMsg));
        }

        brandRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Brand deleted successfully", null));
    }
}
