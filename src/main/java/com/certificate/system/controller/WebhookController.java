package com.certificate.system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/github-webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestBody(required = false) Map<String, Object> payload) {
        
        logger.info("Received GitHub Webhook Event: {}", event);
        if (payload != null && payload.containsKey("repository")) {
            logger.info("Webhook triggered for repository: {}", payload.get("repository"));
        }

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "GitHub Webhook received and processed successfully by Digital Certificate System",
            "event", event != null ? event : "ping"
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> webhookInfo() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "endpoint", "/github-webhook",
            "service", "Digital Certificate System CI/CD Webhook Listener"
        ));
    }
}
