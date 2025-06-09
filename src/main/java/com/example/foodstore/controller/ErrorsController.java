package com.example.foodstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorsController implements ErrorController {
    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");

        // Mã trạng thái lỗi
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        // Thông báo lỗi
        String errorMessage = getErrorMessage(statusCode, message != null ? message.toString() : "");

        // Truyền dữ liệu vào model
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);

        // Luôn trả về trang web/404 cho mọi lỗi
        return "web/404";
    }

    // Hàm tạo thông báo lỗi tùy theo mã trạng thái
    private String getErrorMessage(int statusCode, String defaultMessage) {
        return switch (statusCode) {
            case 400 -> "Yêu cầu không hợp lệ.";
            case 401 -> "Không được phép truy cập.";
            case 403 -> "Truy cập bị từ chối.";
            case 404 -> "Trang không tồn tại hoặc đã bị xóa.";
            case 500 -> "Lỗi máy chủ nội bộ.";
            default -> defaultMessage.isEmpty() ? "Đã xảy ra lỗi không xác định." : defaultMessage;
        };
    }
}