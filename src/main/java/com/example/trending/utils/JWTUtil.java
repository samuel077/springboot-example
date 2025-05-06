package com.example.trending.utils;

import com.example.trending.db.enums.TokenType;
import com.example.trending.db.model.Token;
import com.example.trending.db.repository.TokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;

import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private final TokenRepository tokenRepository;

    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        byte[] keyBytes = Base64.getDecoder().decode(secretKey); // 👈 decode base64 secret
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidity); // e.g., 7 days

        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public void saveRefreshTokenToDb(String tokenStr, Long userId, Date expiresAt) {
        Token token = Token.builder()
                .token(tokenStr)
                .userId(userId)
                .tokenType(TokenType.REFRESH)
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(expiresAt)
                .build();
        tokenRepository.save(token);
    }

    public boolean isRefreshTokenValid(String tokenStr) {
        return tokenRepository.findByToken(tokenStr)
                .filter(token -> !token.isExpired() && !token.isRevoked())
                .filter(token -> token.getExpiresAt().after(new Date()))
                .isPresent();
    }

    public Claims parseToken(String jwtToken) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // key 要 decode base64 過後的
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}