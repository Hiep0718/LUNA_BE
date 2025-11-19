package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CheckoutRequestDTO;
import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.dtos.OrderResponseDTO;
import iuh.fit.se.entities.Orders;
import iuh.fit.se.services.OrderService;
import jakarta.servlet.http.HttpSession;
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

    // Checkout: Tạo đơn hàng từ giỏ hàng
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(
            Authentication auth,
            @RequestBody CheckoutRequestDTO req,
            HttpSession session) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in to checkout"));
        }

        CartDTO cart = (CartDTO) session.getAttribute("CART");
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Cart is empty"));
        }

        try {
            Orders order = orderService.checkout(auth.getName(), cart, req.addressId());
            session.removeAttribute("CART"); // Clear cart after successful checkout
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Order created successfully", OrderResponseDTO.fromEntity(order)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    // Get order history: Lịch sử đơn hàng của user
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<?>> getMyOrders(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            List<Orders> orders = orderService.getOrderHistory(auth.getName());
            List<OrderResponseDTO> dtos = orders.stream()
                    .map(OrderResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(200, "Order history retrieved successfully", dtos));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    // Get order by ID: Chi tiết 1 đơn hàng
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable int orderId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            List<Orders> userOrders = orderService.getOrderHistory(auth.getName());
            Orders order = userOrders.stream()
                    .filter(o -> o.getId() == orderId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            return ResponseEntity.ok(ApiResponse.success(200, "Order retrieved successfully", OrderResponseDTO.fromEntity(order)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }
}
