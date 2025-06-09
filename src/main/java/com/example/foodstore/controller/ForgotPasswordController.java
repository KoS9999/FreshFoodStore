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
            model.addAttribute("error", "OTP không hợp lệ. Vui lòng nhập lại.");
            return "web/confirmOTPfgpw";
        }
    }

    @PostMapping("/forgotpassword")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        boolean emailExists = accountService.sendResetPasswordEmail(email);
        if (emailExists) {
            model.addAttribute("message", "Mã OTP đã được gửi vào email của bạn");
            return "web/confirmOTPfgpw";
        } else {
            model.addAttribute("error", "Email này chưa được đăng ký");
            return "web/forgotpassword";
        }
    }
    @PostMapping("/newPassword")
    public String handleNewPassword(@RequestParam String otp,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword, Model model) {
        if (newPassword.length() < 8) {
            model.addAttribute("error", "Mật khẩu mới phải ít nhất 8 ký tự.");
            model.addAttribute("otp", otp);
            return "web/newpassword";
        }


        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp với nhau.");
            model.addAttribute("otp", otp);
            return "web/newpassword";
        }

        // Gọi service để cập nhật mật khẩu
        boolean isPasswordUpdated = accountService.updatePasswordWithOtp(otp, newPassword);
        if (isPasswordUpdated) {
            model.addAttribute("message", "Mật khẩu của bạn đã được cập nhật thành công");
            return "web/login";
        } else {
            model.addAttribute("error", "Lỗi khi cập nhật mật khẩu");
            return "web/newpassword";
        }
    }


}

