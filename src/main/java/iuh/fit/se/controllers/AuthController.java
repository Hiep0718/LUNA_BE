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

import iuh.fit.se.dtos.ApiResponse;

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
        if (authRequest == null || authRequest.username() == null || authRequest.password() == null
                || authRequest.username().trim().isEmpty() || authRequest.password().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Username and password are required"));
        }

        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody AuthRequest req) {
        if (req == null || req.username() == null || req.password() == null
                || req.username().trim().isEmpty() || req.password().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Username and password are required"));
        }

        if (req.username().length() < 3 || req.password().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Username must be 3+ chars, password must be 6+ chars"));
        }

        if (userService.existsByUsername(req.username())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Username already exists"));
        }

        User user = new User();
        user.setUsername(req.username());
        user.setFullName(req.username());
        user.setEmail(req.email());
        user.setPhone(req.phone());
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

    public record AuthRequest(String username, String password, String fullName, String email, String phone) {}
}
