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
        return "web/forgotpassword";  // Hiển thị trang quên mật khẩu
    }

    @PostMapping("/confirmOtpForgotPassword")
    public String handleConfirmOtp(@RequestParam String otp, Model model) {
        System.out.println("OTP received: " + otp);  // Log OTP nhận được từ form
        boolean isValidOtp = accountService.confirmOtpForgotPassword(otp);
        if (isValidOtp) {
            System.out.println("OTP is valid, proceeding to new password page.");
            model.addAttribute("otp", otp);
            return "web/newpassword";
        } else {
            System.out.println("Invalid OTP, returning to confirm OTP page.");
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "web/confirmOTPfgpw";
        }
    }




    @PostMapping("/forgotpassword")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        boolean emailExists = accountService.sendResetPasswordEmail(email);
        if (emailExists) {
            model.addAttribute("message", "An email has been sent to reset your password.");
            return "web/confirmOTPfgpw";  // Điều hướng đến trang nhập OTP
        } else {
            model.addAttribute("error", "Email not found.");
            return "web/forgotpassword";
        }
    }

    @PostMapping("/newPassword")
    public String handleNewPassword(@RequestParam String otp,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword, Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("otp", otp);  // Để người dùng nhập lại OTP nếu cần thiết
            return "web/newpassword";  // Quay lại trang nếu mật khẩu không khớp
        }

        boolean isPasswordUpdated = accountService.updatePasswordWithOtp(otp, newPassword);
        if (isPasswordUpdated) {
            model.addAttribute("message", "Your password has been updated successfully.");
            return "web/login";  // Chuyển đến trang đăng nhập sau khi đổi mật khẩu thành công
        } else {
            model.addAttribute("error", "Failed to update password.");
            return "web/newpassword";
        }
    }

}

