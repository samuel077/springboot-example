package com.example.trending.service.messging;

import com.example.trending.service.model.message.MfaEmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailQueueService {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public EmailQueueService(SqsTemplate sqsTemplate,
                             ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMfaEmail(Long userId, String email, String mfaCode) {
        try {
            MfaEmailMessage message = new MfaEmailMessage(userId, email, mfaCode);
            String messageBody = objectMapper.writeValueAsString(message);

            sqsTemplate.send(option ->
                    option.queue("mfa-email-queue").payload(messageBody));
            log.info("[debug] sending message successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to send MFA email message to SQS", e);
        }
    }
}