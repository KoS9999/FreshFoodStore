package com.example.foodstore.serviceImpl;

import com.example.foodstore.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendNewOrderNotification(String adminTopic, String message) {
        messagingTemplate.convertAndSend(adminTopic, message);
    }

    @Override
    public void sendNewReviewNotification(String adminTopic, String message) {
        messagingTemplate.convertAndSend(adminTopic, message);
    }
}
