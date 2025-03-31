package com.example.foodstore.controller;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;
import com.example.foodstore.service.OrderDetailService;
import com.example.foodstore.service.OrderService;
import com.example.foodstore.service.ReviewService;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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


        return "web/account";
    }
    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User updatedUser, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getLoggedInUser();
        currentUser.setName(updatedUser.getName());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());
        userService.save(currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Your account details have been updated successfully!");
        return "redirect:/account";
    }
    @GetMapping("/orders/{id}")
    @ResponseBody
    public List<OrderDetail> getOrderDetails(@PathVariable("id") Long id) {
        Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        return orderDetailService.findDetailsByOrder(order);
    }
}




