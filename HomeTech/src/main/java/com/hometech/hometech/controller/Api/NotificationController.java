package com.hometech.hometech.controller.Api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        private String message;
        private String timestamp;
    }

    // STOMP entrypoint for app messages
    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public Notification broadcast(Notification notification) {
        if (notification != null) {
            notification.setTimestamp(LocalDateTime.now().toString());
        }
        return notification;
    }

    // REST helper to trigger a test notification
    @PostMapping("/api/notify")
    public void sendNotification(@RequestBody Notification notification) {
        if (notification == null) {
            notification = new Notification("(trống)", LocalDateTime.now().toString());
        }
        notification.setTimestamp(LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}


