package com.jsalvar.barbilling.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtService {

    // Generate token from user
    String generateToken(UserDetails userDetails);

    // Extract username (subject)
    String extractUsername(String token);

    // Extract roles (custom claim)
    List<String> extractRoles(String token);

    // Validate token against user (Expiration is automatically validated by the framework when .parseSignedClaims(token) is called. throws an ExpiredJwtException
    boolean isTokenValid(String token, UserDetails userDetails);
}
