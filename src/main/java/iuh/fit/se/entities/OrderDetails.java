package iuh.fit.se.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@IdClass(OrderDetailsId.class)
public class OrderDetails {
    @Id // Đánh dấu là 1 phần của khóa chính
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order; // Tên "order" phải khớp với tên trong OrderDetailsId

    @Id // Đánh dấu là phần còn lại của khóa chính
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product; // Tên "product" phải khớp với tên trong OrderDetailsId

    // Các cột dữ liệu bổ sung
    private int quantity;
    private double price; // Rất tốt (lưu giá tại thời điểm mua)
    public OrderDetailsId getOrderDetailsId() {
        return new OrderDetailsId(order.getId(), product.getId());
    }
}

