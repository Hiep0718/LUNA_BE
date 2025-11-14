package iuh.fit.se.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Sẽ là "ROLE_CUSTOMER" và "ROLE_ADMIN"
    public Role(String name) {
        this.name = name;
    }
}
