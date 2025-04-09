package com.example.foodstore.controller;

import com.example.foodstore.entity.*;
import com.example.foodstore.service.*;
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
    public ResponseEntity<String> updateUser(@ModelAttribute("user") User updatedUser) {
        User currentUser = userService.getLoggedInUser();
        currentUser.setName(updatedUser.getName());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());
        userService.save(currentUser);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/orders/{id}")
    @ResponseBody
    public List<OrderDetail> getOrderDetails(@PathVariable("id") Long id) {
        Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return orderDetailService.findDetailsByOrder(order);
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




