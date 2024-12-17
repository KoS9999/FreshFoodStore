package com.example.foodstore.controlleradmin;


import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderStatus;
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
    @GetMapping
    public String viewOrderList(Model model) {
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-list";
    }

    @GetMapping("/update-status/{id}/{status}")
    public String updateOrderPaymentStatus(@PathVariable Long id, @PathVariable int status) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            Order currentOrder = order.get();
            currentOrder.setStatus(status);
            orderService.save(currentOrder);
        }
        return "redirect:/admin/orders";
    }
    @GetMapping("/update-order-status/{id}/{status}")
    public String updateOrderStatus(@PathVariable Long id, @PathVariable String status) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            Order currentOrder = order.get();
            currentOrder.setOrderStatus(OrderStatus.valueOf(status));
            orderService.save(currentOrder);
        }
        return "redirect:/admin/orders";
    }
    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            Order currentOrder = order.get();
            model.addAttribute("order", currentOrder);
                model.addAttribute("orderDetails", currentOrder.getOrderDetails());
            return "admin/order-details";
        } else {
            return "redirect:/admin/orders?error=OrderNotFound";
        }
    }
}
