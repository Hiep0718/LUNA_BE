package iuh.fit.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
// 1. Thêm dòng này để tránh lỗi 500 khi Hibernate load dữ liệu dạng Lazy
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Attributes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @CreationTimestamp
    private Instant createdAt;
    @CreationTimestamp
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;

    @ToString.Exclude
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // 2. QUAN TRỌNG: Thêm @JsonIgnore để ngắt vòng lặp vô tận
    @JsonIgnore
    private List<ProductAttributes> productAttributes;
}