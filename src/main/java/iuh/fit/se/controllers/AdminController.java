package iuh.fit.se.controllers;

import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository; // Injec

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("ADMIN! Đây là trang dashboard quản trị.");
    }

    // Yêu cầu: Xem danh sách tài khoản
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> listUsers() {
        // Lưu ý: Trả về User entity sẽ lộ mật khẩu (đã hash)
        // Nên tạo UserDTO để che mật khẩu đi
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Yêu cầu: Xem chi tiết tài khoản
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Yêu cầu: Cập nhật tài khoản (ví dụ: cập nhật role, email...)
    public record UserUpdateRequest(String email, String fullname, Set<Long> roleIds) {}

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        // (Thêm logic tìm User, tìm Roles từ roleIds,
        // set email, set fullname, set roles... rồi save lại)
        return ResponseEntity.ok(Map.of("message", "User " + id + " updated"));
    }
}
