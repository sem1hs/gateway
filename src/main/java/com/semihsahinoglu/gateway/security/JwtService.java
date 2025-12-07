package com.semihsahinoglu.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public boolean validateToken(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate != null && !expirationDate.before(new Date());
    }

    public List<GrantedAuthority> extractRoles(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = claims.get("roles", List.class);

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    private Date extractExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    private Key getSignKey() {
        String JWT_KEY = jwtProperties.getKey();
        byte[] keyBytes = Decoders.BASE64.decode(JWT_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
