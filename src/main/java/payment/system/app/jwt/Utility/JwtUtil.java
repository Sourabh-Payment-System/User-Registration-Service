package payment.system.app.jwt.Utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import payment.system.app.service.CustomUserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMillis;
    private final String issuer;
    private final String audience;

    public JwtUtil(
            @Value("${jwt.secret-base64}") String secretBase64,
            @Value("${jwt.expiration-ms}") long expirationMillis,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience) {

        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalArgumentException("JWT secret must be configured");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT HMAC key must be at least 256 bits");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(CustomUserDetails principal) {
        Date now = new Date();

        var builder = Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .claim("userId", principal.getUserId())
                .claim("authorities", principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis));

        return builder.signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return parseAndValidate(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }
}
