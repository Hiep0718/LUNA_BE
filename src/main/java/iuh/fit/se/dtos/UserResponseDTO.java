package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private Instant createdAt;
    private Set<String> roles; // Chỉ lấy tên role, không lộ ID
}
