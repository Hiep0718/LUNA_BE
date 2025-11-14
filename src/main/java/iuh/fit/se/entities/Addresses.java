package iuh.fit.se.entities;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Addresses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY) // Nên đổi sang LAZY
    @JoinColumn(name = "user_id")
    private User user; // OK, địa chỉ này của user nào

    private String street;
    private String city;
    private String district;
    private String province;
    private boolean isDefault;

}
