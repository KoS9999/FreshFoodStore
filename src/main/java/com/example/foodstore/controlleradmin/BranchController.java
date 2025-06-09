package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Branch;
import com.example.foodstore.entity.BranchStatus;
import com.example.foodstore.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/branches")
public class BranchController {

    @Autowired
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public String listBranches(Model model) {
        List<Branch> branches = branchService.getAllBranches();
        model.addAttribute("branches", branches);
        model.addAttribute("statuses", BranchStatus.values());
        return "admin/branch-list";
    }

    @GetMapping("/create")
    public String createBranchForm(Model model) {
        model.addAttribute("branch", new Branch());
        model.addAttribute("statuses", BranchStatus.values());
        return "admin/add-branch";
    }

    @PostMapping("/save")
    public String saveBranch(@ModelAttribute Branch branch) {
        branchService.saveBranch(branch);
        return "redirect:/admin/branches";
    }

    @GetMapping("/edit/{id}")
    public String editBranch(@PathVariable Long id, Model model) {
        Branch branch = branchService.getBranchById(id).orElse(new Branch());
        model.addAttribute("branch", branch);
        model.addAttribute("statuses", BranchStatus.values());
        return "admin/edit-branch";
    }

    @PostMapping("/update/{id}")
    public String updateBranch(@PathVariable Long id, @ModelAttribute Branch branch) {
        branchService.updateBranch(id, branch);
        return "redirect:/admin/branches";
    }

    @GetMapping("/delete/{id}")
    public String deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return "redirect:/admin/branches";
    }
}
