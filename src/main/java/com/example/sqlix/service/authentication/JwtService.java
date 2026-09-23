package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration}")
    private long accessExpiration;

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expirationTime
    ) {

        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expirationTime)
                )
                .signWith(getSignInKey())
                .compact();
    }

    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put(
                "scope",
                "ROLE_" + user.getSystemRole().name()
        );

        return buildToken(
                claims,
                user,
                accessExpiration
        );
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, User user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail())
                && !isTokenExpired(token)
                && Boolean.TRUE.equals(user.getIsActive());
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
