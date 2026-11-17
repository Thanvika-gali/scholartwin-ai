package com.aischolartwin.security;

import com.aischolartwin.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT generation, parsing, and validation using HMAC-SHA256.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secretKeyString;
    private final long expirationMs;

    public JwtService(
        @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
        @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secretKeyString = secret;
        this.expirationMs = expirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad safely to 32 bytes (256 bits) if necessary to satisfy HMAC-SHA256 requirements
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT token for the authenticated user.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole().name());
        return createToken(claims, user.getEmail());
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return createToken(extraClaims, subject);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject.toLowerCase().trim())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the subject (email) from the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates whether a token is valid, matches the given email, and is not expired.
     */
    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            final String email = extractEmail(token);
            return (email.equalsIgnoreCase(expectedEmail) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if the token has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}

