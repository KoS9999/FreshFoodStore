package com.example.foodstore.controlleradmin;

import com.example.foodstore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;
    @Autowired
    public AdminController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        notificationService.sendNewOrderNotification("/topic/admin", "Có đơn hàng mới từ khách hàng!");
        notificationService.sendNewReviewNotification("/topic/admin", "Có đánh giá mới từ khách hàng!");
        return "admin/dashboard";
    }
}
