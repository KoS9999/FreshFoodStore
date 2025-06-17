package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderStatus;
import com.example.foodstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
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

    @PostMapping("/update-status/{id}/{status}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderPaymentStatus(@PathVariable Long id, @PathVariable int status) {
        Map<String, Object> response = new HashMap<>();
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            Order currentOrder = order.get();
            currentOrder.setStatus(status);
            orderService.save(currentOrder);
            response.put("status", status);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Order not found");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-order-status/{id}/{status}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            Optional<Order> order = orderService.findById(id);
            if (order.isPresent()) {
                Order currentOrder = order.get();
                currentOrder.setOrderStatus(orderStatus);
                orderService.save(currentOrder);
                response.put("orderStatus", status);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Order not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("error", "Invalid status");
            return ResponseEntity.badRequest().body(response);
        }
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