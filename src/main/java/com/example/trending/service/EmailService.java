package com.example.trending.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMfaEmail(String to, String mfaCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your MFA Verification Code");
        message.setText("Hello,\n\nYour MFA verification code is: " + mfaCode + "\n\nThe code will expire in 5 minutes.\n\nThank you!");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage(), e);
        }
        log.info("[emailService] Email sent to: {}", to);
    }
}