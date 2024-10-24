package com.example.foodstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal User user, Model model) {
        // Thêm thông tin người dùng vào model
        model.addAttribute("username", user.getUsername());
        return "web/account";
    }
}
