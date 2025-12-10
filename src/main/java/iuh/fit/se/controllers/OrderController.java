package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CheckoutRequestDTO;
import iuh.fit.se.dtos.OrderResponseDTO;
import iuh.fit.se.entities.Orders;
import iuh.fit.se.services.EmailService; // Import thêm
import iuh.fit.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException; // Import thêm bắt lỗi DB
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
    private final EmailService emailService; // Inject thêm EmailService

    @Autowired
    public OrderController(OrderService orderService, EmailService emailService) {
        this.orderService = orderService;
        this.emailService = emailService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(Authentication auth, @RequestBody CheckoutRequestDTO req) {
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));

        try {
            // 1. Transaction DB (Lưu Order + Details)
            // Nhờ hàm service đã sửa, 'order' trả về sẽ có đầy đủ Details mà không gây lỗi Hibernate
            Orders order = orderService.checkout(auth.getName(), req.addressId());

            // 2. Gửi Email (Chạy Async, không ảnh hưởng DB)
            try {
                emailService.sendOrderConfirmationEmail(order);
            } catch (Exception ex) {
                System.err.println("⚠ Email error (Order created successfully): " + ex.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Order created successfully", OrderResponseDTO.fromEntity(order)));

        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, "Double Request", "Spam click detected."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Checkout failed", e.getMessage()));
        }
    }

    // --- Các API khác giữ nguyên ---

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

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable int orderId, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }
        try {
            List<Orders> userOrders = orderService.getOrderHistory(auth.getName());
            Orders order = userOrders.stream()
                    .filter(o -> o.getId() == orderId)
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

    @GetMapping("/admin/filter")
    public ResponseEntity<ApiResponse<?>> getOrdersByStatusForAdmin(@RequestParam String status) {
        List<Orders> orders = orderService.getOrdersByStatus(status);
        List<OrderResponseDTO> dtos = orders.stream().map(OrderResponseDTO::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(200, "All orders by status", dtos));
    }

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