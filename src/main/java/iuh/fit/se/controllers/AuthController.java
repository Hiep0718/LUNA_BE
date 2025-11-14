package iuh.fit.se.controllers;

import iuh.fit.se.entities.User;
import iuh.fit.se.security.JwtTokenService;
import iuh.fit.se.services.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        UserDetails ud = (UserDetails) authentication.getPrincipal();

        String token = jwtTokenService.generateToken(ud);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {

        if (userService.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        userService.save(user);

        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out (stateless)"));
    }

    public record AuthRequest(String username, String password) {}
}
