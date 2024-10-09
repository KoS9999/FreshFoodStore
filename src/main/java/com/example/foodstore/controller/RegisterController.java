package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String showRegisterPage() {
        return "web/register"; // Hiển thị trang đăng ký
    }

    @PostMapping
    public String registerUser(@RequestParam String email, @RequestParam String password, Model model) {
        boolean isRegistered = accountService.registerUser(email, password);
        if (isRegistered) {
            model.addAttribute("message", "OTP has been sent to your email. Please confirm.");
            return "web/confirm-otp-register";
        } else {
            model.addAttribute("error", "Registration failed. Email may already exist.");
            return "web/register";
        }
    }

    @PostMapping("/confirm")
    public String confirmOtpRegister(@RequestParam String otp, Model model) {
        boolean isConfirmed = accountService.confirmOtpRegister(otp);
        if (isConfirmed) {
            model.addAttribute("message", "Registration successful!");
            return "web/login";
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "web/confirm-otp-register";
        }
    }
}
