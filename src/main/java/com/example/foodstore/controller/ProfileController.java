package com.example.foodstore.controller;

import com.example.foodstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("orderHistory", orderService.getOrderHistory());
        return "web/profile";  // Trả về trang profile từ thư mục web
    }
}
