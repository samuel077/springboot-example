package com.example.trending.controller;

import com.example.trending.controller.model.LoginRequest;
import com.example.trending.controller.model.auth.RefreshTokenRequest;
import com.example.trending.controller.model.auth.RefreshTokenResponse;
import com.example.trending.controller.model.auth.RegisterRequest;
import com.example.trending.controller.model.auth.VerifyMfaRequest;
import com.example.trending.db.enums.RoleType;
import com.example.trending.db.enums.TokenType;
import com.example.trending.db.model.Token;
import com.example.trending.db.model.User;
import com.example.trending.db.repository.TokenRepository;
import com.example.trending.db.repository.UserRepository;
import com.example.trending.service.AuthService;
import com.example.trending.service.TokenService;
import com.example.trending.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        log.info("[debug] log here: request: {}", req);
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(RoleType.USER);
        userRepository.save(user);

        log.info("[debug] user saved: {}", user);

        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            Claims claims = jwtUtil.extractClaims(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());
            String role = claims.get("role", String.class);

            // 查找 token 是否有效（存在且沒過期且沒被 revoke）
            Optional<Token> storedTokenOpt = tokenRepository.findByTokenAndTokenType(refreshToken, TokenType.REFRESH);
            if (storedTokenOpt.isEmpty() || storedTokenOpt.get().isExpired() || storedTokenOpt.get().isRevoked()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or revoked refresh token");
            }

            // 產生新 access token
            String newAccessToken = jwtUtil.generateAccessToken(userId, null, role);

            // 儲存新 access token
            tokenService.saveToken(userId, newAccessToken, TokenType.ACCESS);

            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, refreshToken));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        authService.login(request.getEmail(), request.getPassword());
        return "Please verify your MFA code sent to your email.";
    }

    @PostMapping("/verify-mfa")
    public RefreshTokenResponse verifyMfa(@RequestBody VerifyMfaRequest request) {
        return authService.verifyMfa(request.getEmail(), request.getMfaCode());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        // 取出 Authorization Header 裡的 Bearer Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.extractClaims(token);
        Long userId = Long.parseLong(claims.getSubject());
        log.info("[claim] userId: {}", userId);

        tokenService.revokeAllTokens(userId);

        return ResponseEntity.ok("Logged out.");
    }
}