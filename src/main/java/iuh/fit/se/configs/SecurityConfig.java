package iuh.fit.se.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtDecoder jwtDecoder;

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtDecoder jwtDecoder) {
        this.userDetailsService = userDetailsService;
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Order(1) // Chain 1: CHỈ xử lý các đường dẫn public
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher( // <-- SỬA LỖI: Chỉ định rõ các đường dẫn cho chain này
                        "/api/auth/**",
                        "/api/products",      // Phải liệt kê cả đường dẫn gốc
                        "/api/products/**",
                        "/api/cart/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",     // <-- Thêm dòng này (cho các file .css, .js)
                        "/swagger-ui.html"  // <-- Thêm dòng này (cho file .html chính)
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Bất kỳ request nào khớp (match) ở trên đều được phép
                );
        // Chain này không bật .oauth2ResourceServer()

        return http.build();
    }

    @Bean
    @Order(2) // Chain 2: Xử lý TẤT CẢ CÁC ĐƯỜNG DẪN CÒN LẠI
    public SecurityFilterChain privateApiFilterChain(HttpSecurity http) throws Exception {
        http
                // Không có .securityMatcher() -> sẽ khớp "any request" không bị chain 1 bắt
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 2. CUSTOMER (phải đăng nhập và có vai trò CUSTOMER)
                        .requestMatchers("/api/orders/checkout").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/my-history").hasRole("CUSTOMER")
                        .requestMatchers("/api/users/me").hasAnyRole("CUSTOMER", "ADMIN")

                        // 3. ADMIN (phải đăng nhập và có vai trò ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. Các yêu cầu còn lại (bắt buộc xác thực)
                        .anyRequest().authenticated()
                )
                // QUAN TRỌNG: Bật xác thực JWT cho chain này
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            jwt.decoder(jwtDecoder);
                            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
                        })
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // Rất quan trọng (để dùng hasRole("ADMIN") thay vì "ROLE_ADMIN")

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*")); // Cho phép tất cả
        // config.setAllowedOrigins(List.of("http://localhost:3000")); // Nếu chạy React/Vue
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}