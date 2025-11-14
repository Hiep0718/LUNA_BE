package iuh.fit.se.controllers;

import iuh.fit.se.entities.OrderDetails;
import iuh.fit.se.entities.OrderDetailsId;
import iuh.fit.se.entities.Orders;
import iuh.fit.se.repositories.OrderDetailsRepository;
import iuh.fit.se.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Yêu cầu: Xem danh sách đơn hàng (sắp xếp theo ngày)
    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAllByOrderByOrderDateDesc());
    }

    // Yêu cầu: Xem chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<Orders> getOrderDetails(@PathVariable int id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Yêu cầu: Cập nhật số lượng mặt hàng trong đơn
    // (Đây là chức năng phức tạp, cần tính lại tổng tiền,
    // kiểm tra lại kho, v.v... Cần 1 service riêng)
    @PutMapping("/{orderId}/items")
    public ResponseEntity<?> updateOrderItemQuantity(
            @PathVariable int orderId,
            @RequestParam int productId,
            @RequestParam int newQuantity) {

        OrderDetailsId id = new OrderDetailsId(orderId, productId);
        Optional<OrderDetails> detailOpt = orderDetailsRepository.findById(id);

        if (detailOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderDetails detail = detailOpt.get();
        // (Thêm logic kiểm tra kho, tính lại tổng tiền cho Order)
        detail.setQuantity(newQuantity);
        orderDetailsRepository.save(detail);

        return ResponseEntity.ok(Map.of("message", "Order item updated"));
    }
}
