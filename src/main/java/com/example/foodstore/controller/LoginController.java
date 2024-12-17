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

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            switch (error) {
                case "emailNotRegistered":
                    model.addAttribute("error", "Email chưa được đăng ký");
                    break;
                case "accountNotEnabled":
                    model.addAttribute("error", "Tài khoản chưa được kích hoạt");
                    break;
                case "invalidCredentials":
                    model.addAttribute("error", "Email hoặc mật khẩu không đúng");
                    break;
                default:
                    model.addAttribute("error", "Đã xảy ra lỗi không xác định");
                    break;
            }
        }
        return "web/login";
    }
}

