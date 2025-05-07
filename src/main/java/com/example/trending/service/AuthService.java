package com.example.trending.service;

import com.example.trending.controller.model.auth.RefreshTokenResponse;
import com.example.trending.controller.model.response.JwtResponse;
import com.example.trending.db.enums.TokenType;
import com.example.trending.db.model.MFACode;
import com.example.trending.db.model.Token;
import com.example.trending.db.model.User;
import com.example.trending.db.repository.MFARepository;
import com.example.trending.db.repository.TokenRepository;
import com.example.trending.db.repository.UserRepository;
import com.example.trending.service.messging.EmailQueueService;
import com.example.trending.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailQueueService emailQueueService;
    private final MfaService mfaService;
    private final MFARepository mfaRepository;
    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepository;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Transactional
    public void login(String email, String password) {
        log.info("[Debug] In login function");
        // 1. 找到 user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // 2. 驗證 password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials.");
        }

        log.info("[debug] login successfully, creating code now");
        // 3. 產生新的 MFA 驗證碼
        String mfaCode = generateMfaCode();

        // 4. 存入 Redis 或資料庫
        mfaService.saveMfaCode(user.getId(), mfaCode);

        // 5. 送一封新 MFA 驗證 email
        emailQueueService.sendMfaEmail(user.getId(), email, mfaCode);
    }

    private String generateMfaCode() {
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    @Transactional
    public RefreshTokenResponse verifyMfa(String email, String mfaCode) {
        log.info("[debug] check mfa with email: {}, and code: {}", email, mfaCode);
        // 1. 查找 User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // 2. 查找該 user 最新一筆 MFA record
        MFACode latestCode = mfaRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new RuntimeException("No MFA code found."));

        log.info("[debug] lastCode: {}", latestCode);

        // 3. 檢查過期
        if (latestCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("MFA code expired.");
        }

        // 4. 檢查 code 是否正確
        if (!latestCode.getCode().equals(mfaCode)) {
            throw new RuntimeException("Invalid MFA code.");
        }

        log.info("[debug] mfa code validated, prepare to remove code");

        // 5. 驗證成功，刪掉使用過的 MFA record
        mfaRepository.deleteById(latestCode.getId());

        // 6. 頒發 JWT token
        String accessToken= jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().toString());

        // save token
        saveAllTokens(user.getId(), accessToken, refreshToken);

        return new RefreshTokenResponse(accessToken, refreshToken);

    }

    public void saveAllTokens(long uid, String accessToken, String refreshToken) {

        log.info("[debug] save to db, accessLength: {}, refreshTokenLength: {}",
                accessTokenValidity, refreshTokenValidity);

        List<Token> tokens = List.of(
                Token.builder()
                        .userId(uid)
                        .token(accessToken)
                        .tokenType(TokenType.ACCESS)
                        .revoked(false)
                        .expired(false)
                        .createdAt(new Date())
                        .expiresAt(new Date(System.currentTimeMillis() + accessTokenValidity))
                        .build(),
                Token.builder()
                        .userId(uid)
                        .token(refreshToken)
                        .tokenType(TokenType.REFRESH)
                        .revoked(false)
                        .expired(false)
                        .createdAt(new Date())
                        .expiresAt(new Date(System.currentTimeMillis() + refreshTokenValidity))
                        .build()
        );

        tokenRepository.saveAll(tokens);
    }
}
