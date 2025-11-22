package iuh.fit.se.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.fit.se.entities.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSimpleDTO(
        Long id,
        String fullname,
        String username,
        String email,
        String phone
) {
    public static UserSimpleDTO fromEntity(User user) {
        if (user == null) return null;
        return new UserSimpleDTO(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
