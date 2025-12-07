package iuh.fit.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@IdClass(ProductAttributesId.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductAttributes {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore // Ngắt vòng lặp
    private Products product;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private Attributes attribute;

    private String value;
}