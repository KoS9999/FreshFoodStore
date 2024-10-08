package com.example.foodstore.controlleradmin;


import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserRepository userRepository;


    @ModelAttribute("user")
    public User user(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
            return user;
        }
        return null;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @GetMapping("/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute("category") Category category) {
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, @ModelAttribute("category") Category category) {
        categoryService.update(id, category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
