package com.example.trending.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

        // 這裡可以加 alert 判斷邏輯，例如只處理 status 為 "firing"
        String message = "[ALERT] Something happened:\n" + payload.toString();
        // ✅ 建議：escape 換行字元與引號
        String safeMessage = message
                .replace("\"", "\\\"")    // escape 雙引號
                .replace("\n", "\\n");    // escape 換行

        String body = """
    {
      "messages": [
        {
          "type": "text",
          "text": "%s"
        }
      ]
    }
    """.formatted(safeMessage);

        // 傳送到 LINE
        sendLineBroadcast("test message from webhook");

        return ResponseEntity.ok("Alert received");
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

