package iuh.fit.se.configs;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecretKey jwtSecretKey() {

        // Kiểm tra xem giá trị có bị null hoặc rỗng không trước khi decode
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret key not found or is empty in application.properties");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            // Cần đảm bảo key dài ít nhất 32 byte (256 bit) cho HMAC-SHA256
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("JWT secret key must be at least 32 bytes long after decoding.");
            }
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT secret key is not a valid Base64 string", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
