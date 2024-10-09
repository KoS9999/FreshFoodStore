package com.example.foodstore.controlleradmin;


import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("user")
    public User user(Model model, Principal principal) {
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();  // Lấy đối tượng User từ Optional
                model.addAttribute("user", user);  // Gán đối tượng User thực tế vào model
                return user;
            }
        }
        return null;  // Trả về null nếu không tìm thấy user
    }

    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        return "admin/order-detail";
    }

    @GetMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long id) {
        orderService.cancelOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/confirm/{id}")
    public String confirmOrder(@PathVariable("id") Long id) {
        orderService.confirmOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/delivered/{id}")
    public String markAsDelivered(@PathVariable("id") Long id) {
        orderService.markAsDelivered(id);
        return "redirect:/admin/orders";
    }
}
