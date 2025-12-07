package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CheckoutRequestDTO;
import iuh.fit.se.dtos.OrderResponseDTO;
import iuh.fit.se.entities.Orders;
import iuh.fit.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Checkout: Tạo đơn hàng từ Database Cart
    // Không cần truyền CartDTO từ Session nữa, Service sẽ tự tìm trong DB
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(
            Authentication auth,
            @RequestBody CheckoutRequestDTO req) {

        // 1. Kiểm tra đăng nhập (Dù SecurityConfig đã chặn, check lại cho chắc)
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            // 2. Gọi Service xử lý
            // auth.getName() lấy username từ JWT Token
            Orders order = orderService.checkout(auth.getName(), req.addressId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Order created successfully", OrderResponseDTO.fromEntity(order)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Checkout failed", e.getMessage()));
        }
    }

    // Get order history
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<?>> getMyOrders(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            List<Orders> orders = orderService.getOrderHistory(auth.getName());
            List<OrderResponseDTO> dtos = orders.stream()
                    .map(OrderResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(200, "Order history retrieved", dtos));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable int orderId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            List<Orders> userOrders = orderService.getOrderHistory(auth.getName());
            Orders order = userOrders.stream()
                    .filter(o -> o.getId() == orderId) // Giả sử ID là int/long tương thích
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            return ResponseEntity.ok(ApiResponse.success(200, "Order details", OrderResponseDTO.fromEntity(order)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }
    @GetMapping("/my-orders/filter")
    public ResponseEntity<ApiResponse<?>> getMyOrdersByStatus(
            Authentication auth,
            @RequestParam String status) {

        if (auth == null) return ResponseEntity.status(401).build();

        List<Orders> orders = orderService.getMyOrdersByStatus(auth.getName(), status);
        List<OrderResponseDTO> dtos = orders.stream().map(OrderResponseDTO::fromEntity).toList();

        return ResponseEntity.ok(ApiResponse.success(200, "Filtered orders", dtos));
    }

    // --- API CHO ADMIN ---

    // Admin lọc tất cả đơn theo trạng thái
    // URL: /api/orders/admin/filter?status=CONFIRMED
    @GetMapping("/admin/filter")
    public ResponseEntity<ApiResponse<?>> getOrdersByStatusForAdmin(@RequestParam String status) {
        List<Orders> orders = orderService.getOrdersByStatus(status);
        List<OrderResponseDTO> dtos = orders.stream().map(OrderResponseDTO::fromEntity).toList();

        return ResponseEntity.ok(ApiResponse.success(200, "All orders by status", dtos));
    }

    // Admin cập nhật trạng thái
    // URL: /api/orders/admin/update-status?orderId=1&status=SHIPPED
    @PutMapping("/admin/update-status")
    public ResponseEntity<ApiResponse<?>> updateOrderStatus(
            @RequestParam int orderId,
            @RequestParam String status) {

        try {
            Orders updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(ApiResponse.success(200, "Status updated", OrderResponseDTO.fromEntity(updatedOrder)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Update failed", e.getMessage()));
        }
    }
}