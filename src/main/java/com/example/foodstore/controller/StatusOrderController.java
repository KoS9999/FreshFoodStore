package com.example.foodstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping
public class StatusOrderController {

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "web/order-success";
    }

    @GetMapping("/order-status")
    public String orderStatus(@RequestParam("status") String status,
                              @RequestParam("message") String message,
                              @RequestParam(value = "txnRef", required = false) String txnRef,
                              Model model) {
        System.out.println("Order Status - Parameters: status=" + status + ", message=" + message + ", txnRef=" + txnRef);
        model.addAttribute("status", status);
        model.addAttribute("message", message);
        model.addAttribute("txnRef", txnRef);
        return "web/order-status";
    }
}
