package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        boolean isEmailSent = accountService.sendResetPasswordEmail(email);
        if (isEmailSent) {
            model.addAttribute("message", "Password reset link has been sent to your email.");
            return "web/forgot-password";
        } else {
            model.addAttribute("error", "Email not found.");
            return "web/forgot-password";
        }
    }

    @PostMapping("/confirm-otp")
    public String confirmOtp(@RequestParam String otp, Model model) {
        boolean isValid = accountService.validateOtp(otp);
        if (isValid) {
            return "web/change-password";
        } else {
            model.addAttribute("error", "Invalid OTP");
            return "web/confirm-otp";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String email, @RequestParam String newPassword, @RequestParam String confirmPassword, Model model) {
        if (newPassword.equals(confirmPassword)) {
            accountService.updatePassword(email, newPassword);
            model.addAttribute("message", "Password changed successfully.");
            return "web/login";
        } else {
            model.addAttribute("error", "Passwords do not match.");
            return "web/change-password";
        }
    }
}
