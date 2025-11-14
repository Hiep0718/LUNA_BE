package iuh.fit.se.controllers;

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
    public ResponseEntity<Brands> createBrand(@RequestBody BrandRequestDTO req) {
        Brands brand = new Brands();
        brand.setName(req.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(brandRepository.save(brand));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Brands>> getAllBrands() {
        return ResponseEntity.ok(brandRepository.findAll());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateBrand(@PathVariable int id, @RequestBody BrandRequestDTO req) {
        return brandRepository.findById(id).map(brand -> {
            brand.setName(req.name());
            brandRepository.save(brand);
            return ResponseEntity.ok((Object) brand);
        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Brand not found"))
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBrandById(@PathVariable int id) {
        return brandRepository.findById(id)
                .map(brand -> ResponseEntity.ok((Object) brand))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "Brand not found"))
                );
    }

    // DELETE (ràng buộc: brand có sản phẩm thì không cho xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable int id) {
        long productCount = brandRepository.countProductsByBrandId(id);

        if (productCount > 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Cannot delete brand. It contains " + productCount + " products."
                    ));
        }

        brandRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Brand deleted successfully"));
    }
}
