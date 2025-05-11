package com.example.trending.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alert")
public class AlertWebhookController {

    @Value("${line.bot.token}")
    private String lineBotToken;

    @PostConstruct
    private void pringLineToken() {
        log.info("linebottoken: {}", lineBotToken);
    }

    private static final String LINE_BROADCAST_API = "https://api.line.me/v2/bot/message/broadcast";

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveAlert(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        log.info("📨 Received alert: {}", payload);

        // 驗證是否為 firing 的 alert
        String status = (String) payload.get("status");
        if (!"firing".equalsIgnoreCase(status)) {
            return ResponseEntity.ok("No firing alerts");
        }

        // 組成訊息
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("[ALERT] 🚨 Firing Alerts Received\n");

        List<Map<String, Object>> alerts = (List<Map<String, Object>>) payload.get("alerts");
        for (Map<String, Object> alert : alerts) {
            Map<String, String> labels = (Map<String, String>) alert.get("labels");
            Map<String, String> annotations = (Map<String, String>) alert.get("annotations");

            String alertName = labels.getOrDefault("alertname", "UnknownAlert");
            String instance = labels.getOrDefault("instance", "unknown-instance");
            String severity = labels.getOrDefault("severity", "unknown");
            String summary = annotations.getOrDefault("summary", "No summary provided.");

            msgBuilder.append("\n🔔 Alert: ").append(alertName)
                    .append("\n🖥 Instance: ").append(instance)
                    .append("\n⚠ Severity: ").append(severity)
                    .append("\n📝 Summary: ").append(summary)
                    .append("\n");
        }

        String safeMessage = msgBuilder.toString()
                .replace("\"", "\\\"")    // escape 雙引號
                .replace("\n", "\\n");    // escape 換行（給 JSON 字串用）

        // 傳送到 LINE
        sendLineBroadcast(safeMessage);

        return ResponseEntity.ok("Firing alerts processed");
    }

    private void sendLineBroadcast(String msg) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(lineBotToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {
              "messages": [
                {
                  "type": "text",
                  "text": "%s"
                }
              ]
            }
            """.formatted(msg.replace("\"", "\\\"")); // escape 雙引號

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(LINE_BROADCAST_API, request, String.class);

        log.info("LINE response: {}", response.getBody());
    }
}

