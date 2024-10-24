package com.example.foodstore.controller;

import com.example.foodstore.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/forgotpassword")
    public String showForgotPasswordPage() {
        return "web/forgotpassword";
    }

    @PostMapping("/confirmOtpForgotPassword")
    public String handleConfirmOtp(@RequestParam String otp, Model model) {
        boolean isValidOtp = accountService.confirmOtpForgotPassword(otp);
        if (isValidOtp) {
            model.addAttribute("otp", otp);
            return "web/newpassword";
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "web/confirmOTPfgpw";
        }
    }

    @PostMapping("/forgotpassword")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        boolean emailExists = accountService.sendResetPasswordEmail(email);
        if (emailExists) {
            model.addAttribute("message", "An email has been sent to reset your password.");
            return "web/confirmOTPfgpw";
        } else {
            model.addAttribute("error", "Email not found.");
            return "web/forgotpassword";
        }
    }
    @PostMapping("/newPassword")
    public String handleNewPassword(@RequestParam String otp,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword, Model model) {
        if (newPassword.length() < 8) {
            model.addAttribute("error", "New password must be at least 8 characters long.");
            model.addAttribute("otp", otp);
            return "web/newpassword";
        }


        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("otp", otp);
            return "web/newpassword";
        }

        // Gọi service để cập nhật mật khẩu
        boolean isPasswordUpdated = accountService.updatePasswordWithOtp(otp, newPassword);
        if (isPasswordUpdated) {
            model.addAttribute("message", "Your password has been updated successfully.");
            return "web/login";
        } else {
            model.addAttribute("error", "Failed to update password.");
            return "web/newpassword";
        }
    }


}

