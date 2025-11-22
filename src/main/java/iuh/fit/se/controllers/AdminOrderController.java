package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.AdminOrderResponseDTO;
import iuh.fit.se.entities.OrderDetails;
import iuh.fit.se.entities.OrderDetailsId;
import iuh.fit.se.entities.Orders;
import iuh.fit.se.repositories.OrderDetailsRepository;
import iuh.fit.se.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    @Autowired
    public AdminOrderController(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    // Xem danh sách đơn hàng (sắp xếp theo ngày)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllOrders() {
        List<Orders> orders = orderRepository.findAllWithDetails();
        List<AdminOrderResponseDTO> dtos = orders.stream()
                .map(AdminOrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminOrderResponseDTO>> getOrderDetails(@PathVariable int id) {
        return orderRepository.findByIdWithDetails(id)
                .map(order -> {
                    // Nhánh 1: Xử lý thành công
                    if (order.getUser() == null || order.getAddress() == null) {

                        // Nhánh 2: Xử lý Lỗi Server (vẫn trả về cùng kiểu Generic)
                        // Sử dụng ApiResponse.error(..., null) để khớp kiểu AdminOrderResponseDTO
                        // Giả định ApiResponse.error có thể trả về kiểu generic mong muốn

                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .<ApiResponse<AdminOrderResponseDTO>>body( // <--- Thêm ép kiểu tường minh ở đây
                                        ApiResponse.error(500, "Server Error", "Order data incomplete")
                                );
                    }

                    // Nhánh 3: Xử lý Thành công (Khớp kiểu)
                    return ResponseEntity.ok(ApiResponse.success(200, "Order retrieved successfully", AdminOrderResponseDTO.fromEntity(order)));
                })
                .orElseGet(() ->
                        // Nhánh 4: Xử lý Lỗi Not Found (vẫn trả về cùng kiểu Generic)

                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .<ApiResponse<AdminOrderResponseDTO>>body( // <--- Thêm ép kiểu tường minh ở đây
                                        ApiResponse.error(404, "Not Found", "Order not found")
                                )
                );
    }

    // Cập nhật số lượng mặt hàng trong đơn
    @PutMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<?>> updateOrderItemQuantity(
            @PathVariable int orderId,
            @RequestParam int productId,
            @RequestParam int newQuantity) {
        if (newQuantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Quantity must be greater than 0"));
        }

        OrderDetailsId id = new OrderDetailsId(orderId, productId);
        var detailOpt = orderDetailsRepository.findById(id);

        if (detailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Order item not found"));
        }

        OrderDetails detail = detailOpt.get();
        detail.setQuantity(newQuantity);
        orderDetailsRepository.save(detail);

        return ResponseEntity.ok(ApiResponse.success(200, "Order item updated successfully", null));
    }
}
