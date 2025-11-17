package iuh.fit.se.controllers;

import iuh.fit.se.entities.User;
import iuh.fit.se.security.JwtTokenService;
import iuh.fit.se.services.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserServiceImpl userService,
                          PasswordEncoder passwordEncoder,
                          JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        UserDetails ud = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(ud);

        var loginResponse = Map.of(
                "token", token,
                "tokenType", "Bearer"
        );
        return ResponseEntity.ok(ApiResponse.success(200, "Login successful", loginResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody AuthRequest req) {
        if (userService.existsByUsername(req.username())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Username already exists"));
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Registration successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> me(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User is not authenticated"));
        }

        var userInfo = Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream().map(Object::toString).collect(Collectors.toList())
        );
        return ResponseEntity.ok(ApiResponse.success(200, "User info retrieved successfully", userInfo));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        return ResponseEntity.ok(ApiResponse.success(200, "Logged out successfully (stateless)", null));
    }

    public record AuthRequest(String username, String password) {}

    // ApiResponse class definition
    public static class ApiResponse<T> {
        private int statusCode;
        private String message;
        private T data;

        public ApiResponse(int statusCode, String message, T data) {
            this.statusCode = statusCode;
            this.message = message;
            this.data = data;
        }

        public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
            return new ApiResponse<>(statusCode, message, data);
        }

        public static ApiResponse<?> error(int statusCode, String message, String error) {
            return new ApiResponse<>(statusCode, message, error);
        }

        // Getters and setters
        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
