package com.example.trending.service.messging;

import com.example.trending.db.model.MFACode;
import com.example.trending.service.EmailService;
import com.example.trending.service.model.message.MfaEmailMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailQueueConsumer {

    private final EmailService emailService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public EmailQueueConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @SqsListener("mfa-email-queue")
    public void handleMessage(String rawMessage) {
        // 收到 message 之後，呼叫 EmailService 發送 email
        //log.info("Object: {}", message.toString());
        MfaEmailMessage message = null;
        try {
            message = objectMapper.readValue(rawMessage, MfaEmailMessage.class);
            log.info("[debug] message: {}", message);
        } catch (JsonProcessingException e) {
            log.error("error: {}", e.getMessage());
        }
        log.info("[consumer] Receive message, email: {}, code: {}", message.getEmail(), message.getMfaCode());
        emailService.sendMfaEmail(message.getEmail(), message.getMfaCode());
    }
}
