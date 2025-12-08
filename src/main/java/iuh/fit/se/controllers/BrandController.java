package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.BrandResponseDTO;
import iuh.fit.se.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands") // Đường dẫn chung, BỎ chữ 'admin'
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    // API lấy tất cả danh sách Brand (Public - Ai cũng xem được)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands() {
        List<BrandResponseDTO> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(200, "Brands retrieved successfully", brands));
    }

    // API lấy chi tiết 1 Brand (Public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrandById(@PathVariable int id) {
        return brandService.getBrandById(id)
                .map(brand -> ResponseEntity.ok(ApiResponse.success(200, "Brand retrieved successfully", brand)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}