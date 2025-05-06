package com.example.trending.controller;

import com.example.trending.controller.model.LoginRequest;
import com.example.trending.controller.model.auth.RefreshTokenRequest;
import com.example.trending.controller.model.auth.RefreshTokenResponse;
import com.example.trending.controller.model.auth.RegisterRequest;
import com.example.trending.controller.model.auth.VerifyMfaRequest;
import com.example.trending.controller.model.response.JwtResponse;
import com.example.trending.db.enums.RoleType;
import com.example.trending.db.model.Token;
import com.example.trending.db.model.User;
import com.example.trending.db.repository.TokenRepository;
import com.example.trending.db.repository.UserRepository;
import com.example.trending.service.AuthService;
import com.example.trending.service.TokenService;
import com.example.trending.service.messging.EmailQueueService;
import com.example.trending.service.MfaService;
import com.example.trending.utils.JWTUtil;
import io.jsonwebtoken.Claims;
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
    private final MfaService mfaService;
    private final EmailQueueService emailQueueService;
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

        if (!tokenService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        Claims claims = tokenService.extractClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        String newAccessToken = jwtUtil.generateAccessToken(userId, email, role);

        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, refreshToken));
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        authService.login(request.getEmail(), request.getPassword());
        return "Please verify your MFA code sent to your email.";
    }

    @PostMapping("/verify-mfa")
    public JwtResponse verifyMfa(@RequestBody VerifyMfaRequest request) {
        return authService.verifyMfa(request.getEmail(), request.getMfaCode());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }

        String jwt = authHeader.substring(7);
        Optional<Token> storedToken = tokenRepository.findByToken(jwt);

        if (storedToken.isPresent()) {
            Token token = storedToken.get();
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
        }

        return ResponseEntity.ok("Logged out");
    }
}