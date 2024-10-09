package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/login")
    public String login() {
        return "web/login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model) {
        // Gọi hàm xác thực từ service và trả về trang tương ứng
        boolean isValidUser = accountService.validateUser(email, password);
        if (isValidUser) {
            return "redirect:/home";  // Chuyển hướng đến trang chủ
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

}
