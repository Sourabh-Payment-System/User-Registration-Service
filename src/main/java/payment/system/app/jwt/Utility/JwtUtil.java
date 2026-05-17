package payment.system.app.jwt.Utility;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Generate signing key
     */
    private Key getSigningKey() {

        logger.debug("Generating JWT signing key");

        if (secret == null || secret.isBlank()) {

            logger.error("JWT secret key is null or empty");

            throw new IllegalArgumentException(
                    "JWT secret key cannot be null or empty");
        }

        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /* ================= TOKEN GENERATION ================= */

    public String generateToken(String username) {

        logger.info(
                "Generating JWT token for username : {}",
                username);

        if (username == null || username.isBlank()) {

            logger.error(
                    "Username is null or empty while generating token");

            throw new IllegalArgumentException(
                    "Username cannot be null or empty");
        }

        try {

            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(
                            new Date(
                                    System.currentTimeMillis()
                                            + expiration))
                    .signWith(
                            getSigningKey(),
                            SignatureAlgorithm.HS256)
                    .compact();

            logger.info(
                    "JWT token generated successfully for username : {}",
                    username);

            return token;

        } catch (Exception ex) {

            logger.error(
                    "Error while generating JWT token for username : {}",
                    username,
                    ex);

            throw new RuntimeException(
                    "Failed to generate JWT token",
                    ex);
        }
    }

    /* ================= CLAIM EXTRACTION ================= */

    public String extractUsername(String token) {

        logger.debug("Extracting username from JWT token");

        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {

        logger.debug("Extracting expiration from JWT token");

        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        if (token == null || token.isBlank()) {

            logger.error(
                    "JWT token is null or empty while extracting claims");

            throw new IllegalArgumentException(
                    "JWT token cannot be null or empty");
        }

        try {

            final Claims claims =
                    extractAllClaims(token);

            logger.debug(
                    "Claims extracted successfully from JWT token");

            return claimsResolver.apply(claims);

        } catch (Exception ex) {

            logger.error(
                    "Error while extracting claim from JWT token",
                    ex);

            throw ex;
        }
    }

    private Claims extractAllClaims(String token) {

        logger.debug("Extracting all claims from JWT token");

        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.debug(
                    "All claims extracted successfully");

            return claims;

        } catch (ExpiredJwtException ex) {

            logger.error(
                    "JWT token has expired : {}",
                    ex.getMessage());

            throw new IllegalArgumentException(
                    "JWT token has expired",
                    ex);

        } catch (MalformedJwtException ex) {

            logger.error(
                    "Malformed JWT token : {}",
                    ex.getMessage());

            throw new IllegalArgumentException(
                    "Malformed JWT token",
                    ex);

        } catch (UnsupportedJwtException ex) {

            logger.error(
                    "Unsupported JWT token : {}",
                    ex.getMessage());

            throw new IllegalArgumentException(
                    "Unsupported JWT token",
                    ex);

        } catch (SecurityException ex) {

            logger.error(
                    "Invalid JWT signature : {}",
                    ex.getMessage());

            throw new IllegalArgumentException(
                    "Invalid JWT signature",
                    ex);

        } catch (Exception ex) {

            logger.error(
                    "Error while extracting JWT claims",
                    ex);

            throw new RuntimeException(
                    "Failed to extract JWT claims",
                    ex);
        }
    }

    /* ================= VALIDATION ================= */

    public boolean isTokenExpired(String token) {

        logger.debug("Checking JWT token expiration");

        boolean isExpired =
                extractExpiration(token)
                        .before(new Date());

        if (isExpired) {

            logger.warn("JWT token is expired");
        }

        return isExpired;
    }

    public boolean validateToken(
            String token,
            String username) {

        logger.info(
                "Validating JWT token for username : {}",
                username);

        if (token == null || token.isBlank()) {

            logger.error(
                    "JWT token is null or empty");

            throw new IllegalArgumentException(
                    "JWT token cannot be null or empty");
        }

        if (username == null || username.isBlank()) {

            logger.error(
                    "Username is null or empty");

            throw new IllegalArgumentException(
                    "Username cannot be null or empty");
        }

        try {

            final String extractedUsername =
                    extractUsername(token);

            boolean isValid =
                    extractedUsername.equals(username)
                            && !isTokenExpired(token);

            if (!isValid) {

                logger.warn(
                        "JWT token validation failed for username : {}",
                        username);
            } else {

                logger.info(
                        "JWT token validated successfully for username : {}",
                        username);
            }

            return isValid;

        } catch (Exception ex) {

            logger.error(
                    "Error while validating JWT token for username : {}",
                    username,
                    ex);

            throw ex;
        }
    }
}