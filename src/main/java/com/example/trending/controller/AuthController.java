package com.example.trending.controller;

import com.example.trending.controller.model.LoginRequest;
import com.example.trending.controller.model.auth.RegisterRequest;
import com.example.trending.controller.model.auth.VerifyMfaRequest;
import com.example.trending.controller.model.response.JwtResponse;
import com.example.trending.db.model.MFACode;
import com.example.trending.db.model.User;
import com.example.trending.db.repository.UserRepository;
import com.example.trending.service.AuthService;
import com.example.trending.service.messging.EmailQueueService;
import com.example.trending.service.MfaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private MfaService mfaService;
    private EmailQueueService emailQueueService;
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        log.info("[debug] log here: request: {}", req);
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        log.info("[debug] user saved: {}", user);

        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        authService.login(request.getEmail(), request.getPassword());
        return "Please verify your MFA code sent to your email.";
    }

    @PostMapping("/verify-mfa")
    public JwtResponse verifyMfa(@RequestBody VerifyMfaRequest request) {
        return JwtResponse.builder()
                .accessToken(authService.verifyMfa(request.getEmail(), request.getMfaCode()))
                .tokenType("Bear")
                .expiresIn(3600)
                .build();
    }
}