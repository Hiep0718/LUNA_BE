package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.BrandRequestDTO;
import iuh.fit.se.dtos.BrandResponseDTO;
import iuh.fit.se.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/brands")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandController {

    private final BrandService brandService;

    @Autowired
    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBrand(@RequestBody BrandRequestDTO req) {
        BrandResponseDTO brand = brandService.createBrand(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Brand created successfully", brand));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands() {
        List<BrandResponseDTO> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(200, "Brands retrieved successfully", brands));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> updateBrand(@PathVariable int id, @RequestBody BrandRequestDTO req) {
        BrandResponseDTO brand = brandService.updateBrand(id, req);
        return ResponseEntity.ok(ApiResponse.success(200, "Brand updated successfully", brand));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrandById(@PathVariable int id) {
        return brandService.getBrandById(id)
                .map(brand -> ResponseEntity.ok(ApiResponse.success(200, "Brand retrieved successfully", brand)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(404, "Not Found", "Brand not found"))
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBrand(@PathVariable int id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok(ApiResponse.success(200, "Brand deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Brand not found"));
        }
    }
}
