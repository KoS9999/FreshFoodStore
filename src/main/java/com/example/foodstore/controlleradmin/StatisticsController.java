package com.example.foodstore.controlleradmin;

import com.example.foodstore.repository.OrderDetailRepository;
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

    @GetMapping("/report-order-status")
    public String reportOrderStatus(Model model) {
        List<Object[]> report = orderDetailRepository.repoByOrderStatus();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Trạng thái đơn hàng");
        return "admin/statistical";
    }

    @GetMapping("/report-voucher")
    public String reportVoucher(Model model) {
        List<Object[]> report = orderDetailRepository.repoByVoucher();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Voucher");
        return "admin/statistical";
    }

    @GetMapping("/report-repeat-purchase")
    public String reportRepeatPurchase(Model model) {
        List<Object[]> report = orderDetailRepository.repoRepeatPurchase();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Tỷ lệ mua lại");
        return "admin/statistical";
    }

    @GetMapping("/report-seasonal")
    public String reportSeasonal(Model model) {
        List<Object[]> report = orderDetailRepository.repoSeasonalProducts();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Sản phẩm theo mùa");
        return "admin/statistical";
    }

    @GetMapping("/report-region")
    public String reportRegion(Model model) {
        List<Object[]> report = orderDetailRepository.repoByRegion();
        model.addAttribute("reportData", report);
        model.addAttribute("reportType", "Khu vực");
        return "admin/statistical";
    }

    @GetMapping("/charts")
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
        return "admin/charts";
    }
}