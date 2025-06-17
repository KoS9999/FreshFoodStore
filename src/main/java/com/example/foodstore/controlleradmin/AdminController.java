package com.example.foodstore.controlleradmin;

import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    public AdminController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String showCharts(Model model) {
        List<Object[]> productData = orderDetailRepository.repo();
        List<Object[]> categoryData = orderDetailRepository.repoWhereCategory();
        List<Object[]> yearData = orderDetailRepository.repoWhereYear();
        List<Object[]> orderStatusData = orderDetailRepository.repoByOrderStatus();
        List<Object[]> voucherData = orderDetailRepository.repoByVoucher();
        List<Object[]> repeatPurchaseData = orderDetailRepository.repoRepeatPurchase();
        List<Object[]> seasonalData = orderDetailRepository.repoSeasonalProducts();
        List<Object[]> regionData = orderDetailRepository.repoByRegion();

        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("productDataJson", mapper.writeValueAsString(productData));
            model.addAttribute("categoryDataJson", mapper.writeValueAsString(categoryData));
            model.addAttribute("yearDataJson", mapper.writeValueAsString(yearData));
            model.addAttribute("orderStatusDataJson", mapper.writeValueAsString(orderStatusData));
            model.addAttribute("voucherDataJson", mapper.writeValueAsString(voucherData));
            model.addAttribute("repeatPurchaseDataJson", mapper.writeValueAsString(repeatPurchaseData));
            model.addAttribute("seasonalDataJson", mapper.writeValueAsString(seasonalData));
            model.addAttribute("regionDataJson", mapper.writeValueAsString(regionData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "admin/dashboard";
    }
}
