package com.hometech.hometech.controller.Thymleaf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationTestController {
    
    @GetMapping("/test-notification")
    public String testNotificationPage() {
        return "test-notification";
    }
}

