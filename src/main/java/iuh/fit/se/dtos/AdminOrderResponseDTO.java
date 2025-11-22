package iuh.fit.se.dtos;

import iuh.fit.se.entities.Orders;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record AdminOrderResponseDTO(
        int id,
        String userName,
        Long userId,
        Instant orderDate,
        String status,
        double subtotal,
        double tax,
        double shippingFee,
        double total,
        String addressStreet,
        String addressCity,
        String addressDistrict,
        String addressProvince,
        List<OrderDetailDTO> items
) {
    public static AdminOrderResponseDTO fromEntity(Orders order) {
        var items = order.getOrderDetails().stream()
                .map(od -> new OrderDetailDTO(
                        od.getProduct().getId(),
                        od.getProduct().getName(),
                        od.getPrice(),
                        od.getQuantity()
                ))
                .collect(Collectors.toList());

        return new AdminOrderResponseDTO(
                order.getId(),
                order.getUser().getUsername(),
                order.getUser().getId(),
                order.getOrderDate(),
                order.getStatus(),
                Double.parseDouble(order.getSubtotal() != null ? order.getSubtotal() : "0"),
                order.getTax(),
                order.getShippingFee(),
                order.getTotal(),
                order.getAddress().getStreet(),
                order.getAddress().getCity(),
                order.getAddress().getDistrict(),
                order.getAddress().getProvince(),
                items
        );
    }
}
