 package iuh.fit.se.dtos;

import lombok.Builder;
import java.util.List;

@Builder
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        List<String> roles
) {}