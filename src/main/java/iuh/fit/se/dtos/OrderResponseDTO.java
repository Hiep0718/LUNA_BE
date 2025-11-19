package iuh.fit.se.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.fit.se.entities.Orders;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponseDTO(
        int id,
        Instant orderDate,
        String status,
        double total,
        double tax,
        double shippingFee,
        String addressStreet,
        String addressCity,
        List<OrderDetailDTO> items
) {
    public static OrderResponseDTO fromEntity(Orders order) {
        var items = order.getOrderDetails().stream()
                .map(od -> new OrderDetailDTO(
                        od.getProduct().getId(),
                        od.getProduct().getName(),
                        od.getPrice(),
                        od.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotal(),
                order.getTax(),
                order.getShippingFee(),
                order.getAddress().getStreet(),
                order.getAddress().getCity(),
                items
        );
    }
}
