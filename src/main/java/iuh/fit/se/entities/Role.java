package iuh.fit.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Fix lỗi Proxy an toàn
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Sẽ là "ROLE_CUSTOMER" và "ROLE_ADMIN"

    public Role(String name) {
        this.name = name;
    }

    public String getRoleName() {
        return this.name;
    }
}