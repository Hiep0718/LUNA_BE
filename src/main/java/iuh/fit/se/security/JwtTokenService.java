package iuh.fit.se.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final long expirationMs = 3600_000;

    @Autowired
    public JwtTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateToken(UserDetails ud) {
        Instant now = Instant.now();

        CustomUserDetails cud = (CustomUserDetails) ud;   // ép kiểu
        Long userId = cud.getUser().getId();             // lấy userId

        List<String> authorities = ud.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .subject(ud.getUsername())
                .claim("id", userId)                     // 👉 Thêm userId vào token
                .claim("authorities", authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

}

