package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;
    @GetMapping("/register")
    public String showRegisterPage() {
        return "web/register";
    }
    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmpassword,
                               Model model) {
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "web/register";
        }
        if (!password.equals(confirmpassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "web/register";
        }

        boolean isRegistered = accountService.registerUser(name, email, password);
        if (isRegistered) {
            model.addAttribute("message", "OTP has been sent to your email.");
            return "web/confirmOTPregister";
        } else {
            model.addAttribute("error", "Email already exists.");
            return "web/register";
        }
    }

    @PostMapping("/confirmOTPregister")
    public String confirmOtpRegister(@RequestParam String otp, Model model) {
        boolean isConfirmed = accountService.confirmOtpRegister(otp);
        if (isConfirmed) {
            model.addAttribute("message", "Registration successful!");
            return "web/login";
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "web/confirmOTPregister";
        }
    }
}
