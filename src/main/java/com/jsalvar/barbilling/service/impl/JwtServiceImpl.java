package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationSeconds}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret));
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(getSigningKey())
                .compact();    }

    // Extract all claims (single parsing point)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public List<String> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);
        return roles;
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();

            return username.equals(userDetails.getUsername());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
