package com.carmats.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String DEFAULT_DEV_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this(jwtProperties, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JwtService(JwtProperties jwtProperties, @Nullable Environment environment) {
        this.jwtProperties = jwtProperties;
        String secret = jwtProperties.getSecret();

        boolean isProd = environment != null && (
                environment.acceptsProfiles(Profiles.of("prod")) ||
                Arrays.asList(environment.getActiveProfiles()).contains("prod")
        );

        if (isProd) {
            if (secret == null || secret.isBlank() || DEFAULT_DEV_SECRET.equals(secret.trim())) {
                throw new IllegalStateException(
                        "PRODUCTION SECURITY ERROR: Default or empty JWT secret is forbidden in production profile. " +
                        "Please configure a strong, unique JWT_SECRET environment variable."
                );
            }
            if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException(
                        "PRODUCTION SECURITY ERROR: JWT secret must be at least 256 bits (32 bytes) in production profile."
                );
            }
        }

        byte[] keyBytes = (secret != null ? secret : DEFAULT_DEV_SECRET).getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad or hash to ensure 256-bit key
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            this.signingKey = Keys.hmacShaKeyFor(padded);
        } else {
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userDetails.getId().toString());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        extraClaims.put("roles", roles);

        return buildToken(extraClaims, userDetails.getUsername(), jwtProperties.getAccessTokenExpirationMs());
    }

    public String generateAccessToken(UUID userId, String email, Collection<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId.toString());
        extraClaims.put("roles", new ArrayList<>(roles));

        return buildToken(extraClaims, email, jwtProperties.getAccessTokenExpirationMs());
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expirationMs
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equalsIgnoreCase(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration != null && expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpirationMs();
    }

    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshTokenExpirationMs();
    }
}
