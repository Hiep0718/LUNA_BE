package iuh.fit.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Fix lỗi 500
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private boolean isActive;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    // Không thêm @JsonIgnore ở đây để Frontend còn lấy được tên Category
    private Categories category;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    // Không thêm @JsonIgnore ở đây để Frontend còn lấy được tên Brand
    private Brands brand;

    @CreationTimestamp
    private Instant createdAt;
    @CreationTimestamp
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    // Giữ lại list này để hiển thị chi tiết sản phẩm
    List<ProductAttributes> productAttributes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    // Giữ lại list này để hiển thị ảnh
    List<ProductImages> productImages = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    // Có thể @JsonIgnore nếu load quá nặng, nhưng tạm thời giữ
    List<Reviews> reviews = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore // QUAN TRỌNG: Nên ẩn đơn hàng khi xem sản phẩm để bảo mật và tránh nặng
    private List<OrderDetails> orderLines = new ArrayList<>();
}