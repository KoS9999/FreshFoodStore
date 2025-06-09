package com.example.foodstore.controller;

import com.example.foodstore.controlleradmin.UserController;
import com.example.foodstore.entity.*;
import com.example.foodstore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AttendanceService attendanceService;

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @GetMapping
    public String viewAccount(Model model) {
        User user = userService.getLoggedInUser();
        model.addAttribute("user", user);

        List<Order> orders = orderService.findOrdersByUser(user);
        model.addAttribute("orders", orders);

        Map<Long, List<OrderDetail>> orderDetailsMap = orders.stream()
                .collect(Collectors.toMap(
                        Order::getOrderId,
                        order -> orderDetailService.findDetailsByOrder(order)
                ));
        model.addAttribute("orderDetailsMap", orderDetailsMap);
        Map<Long, List<Review>> reviewMap = orders.stream()
                .flatMap(order -> orderDetailService.findDetailsByOrder(order).stream())
                .map(orderDetail -> reviewService.getReviewsByUserAndOrderDetail(user, orderDetail))
                .filter(reviews -> reviews != null && !reviews.isEmpty()) // Bỏ qua nếu chưa có đánh giá
                .collect(Collectors.toMap(reviews -> reviews.get(0).getOrderDetail().getOrderDetailId(), reviews -> reviews));

        model.addAttribute("reviewMap", reviewMap);
        List<Attendance> recentAttendances = attendanceService.getRecentAttendances(user, 7);
        model.addAttribute("recentAttendances", recentAttendances);

        long totalCheckIn = attendanceService.countDaysCheckedIn(user);
        model.addAttribute("totalCheckIn", totalCheckIn);


        return "web/account";
    }
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateUser(@ModelAttribute("user") User updatedUser, HttpSession session) {
        User currentUser = userService.getLoggedInUser();
        currentUser.setName(updatedUser.getName());
        currentUser.setAddress(updatedUser.getAddress());
        userService.save(currentUser);
        session.setAttribute("smsAttempts", 0);
        return ResponseEntity.ok("Cập nhật thành công");
    }
    @PostMapping("/update-phone")
    @ResponseBody
    public ResponseEntity<String> updatePhone(@RequestParam String phone,
                                              @RequestParam String verificationCode,
                                              HttpSession session) {
        User currentUser = userService.getLoggedInUser();
        String storedCode = (String) session.getAttribute("verificationCode");
        String phoneToVerify = (String) session.getAttribute("phoneToVerify");
        Long codeCreatedAt = (Long) session.getAttribute("codeCreatedAt");

        logger.info("Debug - Current phone: " + currentUser.getPhone());
        logger.info("Debug - Updated phone: " + phone);
        logger.info("Debug - Received verificationCode: " + verificationCode);
        logger.info("Debug - Stored OTP: " + storedCode);
        logger.info("Debug - Phone to verify: " + phoneToVerify);
        logger.info("Debug - Code created at: " + (codeCreatedAt != null ? new java.util.Date(codeCreatedAt) : "null"));
        logger.info("Debug - Current time: " + new java.util.Date());

        if (storedCode == null || !storedCode.equals(verificationCode)) {
            logger.info("Debug - OTP không khớp");
            return ResponseEntity.badRequest().body("Mã xác thực không đúng");
        }

        if (phoneToVerify == null || !phoneToVerify.equals(phone)) {
            logger.info("Debug - Số điện thoại không khớp");
            return ResponseEntity.badRequest().body("Số điện thoại không khớp với mã xác thực");
        }

        if (codeCreatedAt == null || (System.currentTimeMillis() - codeCreatedAt) > 10 * 60 * 1000) {
            logger.info("Debug - OTP hết hạn");
            session.removeAttribute("verificationCode");
            session.removeAttribute("phoneToVerify");
            session.removeAttribute("codeCreatedAt");
            return ResponseEntity.badRequest().body("Mã xác thực đã hết hạn");
        }

        currentUser.setPhone(phone);
        userService.save(currentUser);

        session.removeAttribute("verificationCode");
        session.removeAttribute("phoneToVerify");
        session.setAttribute("smsAttempts", 0);
        session.removeAttribute("codeCreatedAt");
        session.removeAttribute("lastAttemptTime");

        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/orders/{id}")
    @ResponseBody
    public List<OrderDetail> getOrderDetails(@PathVariable("id") Long id) {
        Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return orderDetailService.findDetailsByOrder(order);
    }

    @PostMapping("/cancel-order/{id}")
    @ResponseBody
    public ResponseEntity<String> cancelOrder(@PathVariable("id") Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("Hủy đơn hàng thành công");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
        }
    }

    @GetMapping("/check-in")
    @ResponseBody
    public Map<String, Object> checkInAjax() {
        boolean success = attendanceService.checkInToday();

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (success) {
            response.put("message", "Điểm danh thành công! +1000 điểm");
        } else {
            response.put("message", "Bạn đã điểm danh hôm nay rồi!");
        }

        return response;
    }
}




