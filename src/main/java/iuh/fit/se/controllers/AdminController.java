package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.UserResponseDTO;
import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.RoleRepository;
import iuh.fit.se.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> adminDashboard() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Dashboard loaded successfully")
                        .data("Welcome to Admin Dashboard")
                        .build()
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> listUsers() {
        List<UserResponseDTO> users = userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build()
        );
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.builder()
                                .status(200)
                                .message("User retrieved successfully")
                                .data(convertToDTO(user))
                                .build()
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .status(404)
                                .message("User not found")
                                .build()
                ));
    }

    public record UserUpdateRequest(String email, String fullname, Set<Long> roleIds) {}

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEmail(req.email());
                    user.setFullname(req.fullname());

                    if (req.roleIds() != null && !req.roleIds().isEmpty()) {
                        Set<Role> roles = req.roleIds()
                                .stream()
                                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                                .collect(Collectors.toSet());
                        user.setRoles(roles);
                    }

                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(
                            ApiResponse.builder()
                                    .status(200)
                                    .message("User updated successfully")
                                    .data(convertToDTO(updatedUser))
                                    .build()
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .status(404)
                                .message("User not found")
                                .build()
                ));
    }

    // Helper method to convert User entity to UserResponseDTO
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullname(user.getFullname());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()));
        return dto;
    }
}
