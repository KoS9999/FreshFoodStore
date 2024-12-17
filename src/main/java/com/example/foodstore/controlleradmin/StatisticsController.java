package com.example.foodstore.controlleradmin;

import com.example.foodstore.repository.OrderDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class StatisticsController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @GetMapping("/report-product")
    public String reportProduct(Model model) {
        List<Object[]> report = orderDetailRepository.repo();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Sản phẩm");
        return "admin/statistical";
    }
    @GetMapping("/report-category")
    public String reportCategory(Model model) {
        List<Object[]> report = orderDetailRepository.repoWhereCategory();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Thể loại");
        return "admin/statistical";
    }
    @GetMapping("/report-year")
    public String reportYear(Model model) {
        List<Object[]> report = orderDetailRepository.repoWhereYear();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Năm");
        return "admin/statistical";
    }
    @GetMapping("/report-month")
    public String reportMonth(Model model) {
        List<Object[]> report = orderDetailRepository.repoWhereMonth();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Tháng");
        return "admin/statistical";
    }
    @GetMapping("/report-quarter")
    public String reportQuarter(Model model) {
        List<Object[]> report = orderDetailRepository.repoWhereQuarter();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Quý");
        return "admin/statistical";
    }
    @GetMapping("/report-customer")
    public String reportCustomer(Model model) {
        List<Object[]> report = orderDetailRepository.reportCustomer();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Khách hàng");
        return "admin/statistical";
    }
    @GetMapping("/charts")
    public String showCharts(Model model) {
        List<Object[]> productData = orderDetailRepository.repo();
        List<Object[]> categoryData = orderDetailRepository.repoWhereCategory();
        List<Object[]> yearData = orderDetailRepository.repoWhereYear();
        // Chuyển đổi dữ liệu sang JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            String productDataJson = mapper.writeValueAsString(productData);
            String categoryDataJson = mapper.writeValueAsString(categoryData);
            String yearDataJson = mapper.writeValueAsString(yearData);
            model.addAttribute("productDataJson", productDataJson);
            model.addAttribute("categoryDataJson", categoryDataJson);
            model.addAttribute("yearDataJson", yearDataJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "admin/charts";
    }
}
