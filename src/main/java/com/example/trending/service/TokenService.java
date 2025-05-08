package com.example.trending.service;

import com.example.trending.db.enums.TokenType;
import com.example.trending.db.model.Token;
import com.example.trending.db.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity; // e.g., 604800000L for 7 days

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);

            if (tokenRepository.findByTokenAndTokenType(token, TokenType.ACCESS).filter(t -> !t.isRevoked() && !t.isExpired()).isPresent())
            {
                log.info("token is valid, expire: {}", claims.getExpiration());
                return claims.getExpiration().after(new Date());
            }
            else {
                log.info("no record");
                return false;
            }

        } catch (JwtException e) {
            log.error("[exception] e: {}", e.getMessage());
            return false;
        }
    }

    public void saveToken(Long userId, String tokenStr, TokenType type) {
        Token token = Token.builder()
                .userId(userId)
                .token(tokenStr)
                .tokenType(type)
                .revoked(false)
                .expired(false)
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + (
                        type == TokenType.ACCESS ? accessTokenValidity : refreshTokenValidity
                )))
                .build();

        tokenRepository.save(token);
    }

    public void revokeAllTokens(Long userId) {
        List<Token> tokens = tokenRepository.findAllByUserIdAndExpiredFalseAndRevokedFalse(userId);

        if (tokens.isEmpty()) return;

        tokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });

        tokenRepository.saveAll(tokens);
    }
}