package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Voucher;
import com.example.foodstore.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public String listVouchers(Model model) {
        List<Voucher> vouchers = voucherService.findAll();
        model.addAttribute("vouchers", vouchers);
        return "admin/voucher";
    }

    @GetMapping("/add")
    public String showAddVoucherForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/add-voucher";
    }

    @PostMapping("/create")
    public String createVoucher(@ModelAttribute Voucher voucher) {
        voucherService.save(voucher);
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/update")
    public String updateVoucher(@ModelAttribute Voucher voucher) {
        voucherService.save(voucher);
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String showEditVoucherForm(@PathVariable Long id, Model model) {
        Optional<Voucher> voucherOpt = voucherService.findById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            String formattedStartDate = voucher.getStartDate() != null ?
                    voucher.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
            String formattedEndDate = voucher.getEndDate() != null ?
                    voucher.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
            model.addAttribute("voucher", voucher);
            model.addAttribute("formattedStartDate", formattedStartDate);
            model.addAttribute("formattedEndDate", formattedEndDate);

            return "admin/edit-voucher";
        }
        return "redirect:/admin/vouchers";
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteById(id);
        return ResponseEntity.ok("Voucher deleted successfully");
    }
}
