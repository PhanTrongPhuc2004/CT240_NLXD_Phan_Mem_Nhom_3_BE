//src/main/java/com/nhom3/ct240/util/JwtUtil.java
package com.nhom3.ct240.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for handling JWT operations (compatible with JJWT 0.12+ / 0.13+):
 * - Generate token with username + roles
 * - Extract username, expiration, roles
 * - Validate token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // ms, ví dụ: 86400000 = 24h
    private Long expiration;

    // Tạo SecretKey an toàn từ secret string (phải dài >= 32 ký tự cho HS256)
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret phải dài ít nhất 32 ký tự để đảm bảo an toàn HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Trích xuất tất cả claims từ token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Trích xuất một claim bất kỳ
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Trích xuất danh sách roles từ claims (key "roles")
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .filter(obj -> obj instanceof String)
                    .map(obj -> (String) obj)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Tạo JWT token chứa username và danh sách roles
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Lấy roles từ UserDetails
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", roles);

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        final Date now = new Date(System.currentTimeMillis());

        return Jwts.builder()
                .claims(claims)  // setClaims thay vì setClaims cũ
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())  // signWith(SecretKey) – không cần SignatureAlgorithm nữa
                .compact();
    }

    /**
     * Validate token với UserDetails (kiểm tra username + hết hạn)
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            // Có thể log: ExpiredJwtException, MalformedJwtException, SignatureException,...
            return false;
        }
    }

    /**
     * Validate token đơn giản (không cần UserDetails) – dùng trong filter
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Nếu parse thành công → hợp lệ
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}