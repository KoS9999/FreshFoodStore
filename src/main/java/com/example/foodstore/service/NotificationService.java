package com.example.foodstore.service;

public interface NotificationService {
    void sendNewOrderNotification(String adminTopic, String message);
    void sendNewReviewNotification(String adminTopic, String message);
}
