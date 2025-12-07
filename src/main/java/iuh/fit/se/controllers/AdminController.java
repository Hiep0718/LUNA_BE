package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.UserResponseDTO;
import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.OrderRepository;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.repositories.RoleRepository;
import iuh.fit.se.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import iuh.fit.se.dtos.ChartDTO;
import iuh.fit.se.dtos.ProductReportDTO;
import iuh.fit.se.entities.Products;
import iuh.fit.se.entities.ProductImages; // Import này
import iuh.fit.se.repositories.OrderRepository;
import iuh.fit.se.repositories.ProductRepository;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository; // Inject thêm cái này

    @Autowired
    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository,
                           OrderRepository orderRepository,
                           ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> adminDashboard() {
        Map<String, Object> responseData = new HashMap<>();

        // 1. Thống kê số lượng đơn (CARD)
        Map<String, Long> summary = new HashMap<>();
        summary.put("pending", orderRepository.countByStatus("PENDING"));
        summary.put("shipping", orderRepository.countByStatus("SHIPPED"));
        summary.put("delivered", orderRepository.countByStatus("DELIVERED"));
        summary.put("cancelled", orderRepository.countByStatus("CANCELLED"));
        summary.put("totalUsers", userRepository.count());
        responseData.put("summary", summary);

        // 2. Biểu đồ (CHART) - Gọi hàm vừa thêm bên Repo
        List<ChartDTO> dailyChart = orderRepository.getRevenueLast7Days().stream()
                .map(obj -> new ChartDTO((String) obj[0], ((Number) obj[1]).doubleValue()))
                .collect(Collectors.toList());
        responseData.put("revenueByDay", dailyChart);

        List<ChartDTO> monthlyChart = orderRepository.getRevenueByMonthCurrentYear().stream()
                .map(obj -> new ChartDTO((String) obj[0], ((Number) obj[1]).doubleValue()))
                .collect(Collectors.toList());
        responseData.put("revenueByMonth", monthlyChart);

        // 3. Top sản phẩm & Xử lý ảnh (TOP SELLING)
        List<Products> topProducts = productRepository.findTopSellingProducts(PageRequest.of(0, 5));
        List<ProductReportDTO> topSellingDTOs = new ArrayList<>();

        for (Products p : topProducts) {
            ProductReportDTO dto = new ProductReportDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setPrice(p.getPrice());

            // --- ĐOẠN NÀY KHÁC VÌ PHẢI LẤY ẢNH TỪ LIST PRODUCT IMAGES CỦA BẠN ---
            if (p.getProductImages() != null && !p.getProductImages().isEmpty()) {
                // Lấy ảnh đầu tiên tìm thấy
                dto.setImage(p.getProductImages().get(0).getImageUrl());
            }

            // Tính tổng số lượng bán & Doanh thu của SP này
            long sold = 0;
            double revenue = 0;
            for (var od : p.getOrderLines()) {
                if ("DELIVERED".equals(od.getOrder().getStatus())) {
                    sold += od.getQuantity();
                    revenue += (od.getQuantity() * od.getPrice());
                }
            }
            dto.setTotalSold(sold);
            dto.setTotalRevenue(revenue);
            topSellingDTOs.add(dto);
        }
        responseData.put("topSellingProducts", topSellingDTOs);

        // 4. Sản phẩm sắp hết hàng
        responseData.put("lowStockProducts", productRepository.findByStockQuantityLessThanEqual(10));

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Dashboard loaded")
                .data(responseData)
                .build());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> listUsers() {
        List<UserResponseDTO> users = userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build()
        );
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.builder()
                                .status(200)
                                .message("User retrieved successfully")
                                .data(convertToDTO(user))
                                .build()
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .status(404)
                                .message("User not found")
                                .build()
                ));
    }

    public record UserUpdateRequest(String email, String fullname, Set<Long> roleIds) {}

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEmail(req.email());
                    user.setFullName(req.fullname());

                    if (req.roleIds() != null && !req.roleIds().isEmpty()) {
                        Set<Role> roles = req.roleIds()
                                .stream()
                                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                                .collect(Collectors.toSet());
                        user.setRoles(roles);
                    }

                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(
                            ApiResponse.builder()
                                    .status(200)
                                    .message("User updated successfully")
                                    .data(convertToDTO(updatedUser))
                                    .build()
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .status(404)
                                .message("User not found")
                                .build()
                ));
    }

    // Helper method to convert User entity to UserResponseDTO
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullname(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()));
        return dto;
    }
}
