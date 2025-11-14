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
public class Discounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String code;
    private double percentage;
    private String name;
    private String description;
    @CreationTimestamp
    private Instant startDate;
    @CreationTimestamp
    private Instant endDate;
    private boolean isActive;
    @CreationTimestamp
    private Instant createdAt;
    @CreationTimestamp
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
    // THÊM PHẦN NÀY (để xem 1 mã được dùng cho bao nhiêu đơn):
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
    private List<Orders> orders = new ArrayList<>();
}
