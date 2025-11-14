package iuh.fit.se.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Sẽ tạo cột 'user_id' trong bảng Orders
    private User user;
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipments shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id") // Sẽ tạo cột 'address_id' trong bảng Orders
    private Addresses address;
    @CreationTimestamp
    private Instant orderDate;
    private String status;
    private String subtotal;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id") // Sẽ tạo cột 'discount_id' trong bảng Orders
    private Discounts discount;
    private double tax;
    private double shippingFee;
    private double total;
    // mappedBy = "order" (trỏ tới trường private Orders order; trong OrderDetails)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> orderDetails = new ArrayList<>();
    @CreationTimestamp
    private Instant createdAt;
    @CreationTimestamp
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
