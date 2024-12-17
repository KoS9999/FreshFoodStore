package com.example.foodstore.controller;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping({"/", "/index"})
    public String index(@RequestParam(defaultValue = "") String keyword, Model model, HttpSession session) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);


        if (!categories.isEmpty()) {
            Long defaultCategoryId = categories.get(0).getCategoryId();
            List<Product> products = productService.findProductsByCategoryId(defaultCategoryId);
            model.addAttribute("products", products);
        }

        Integer totalCartItems = (Integer) session.getAttribute("totalCartItems");
        if (totalCartItems == null) {
            totalCartItems = 0;
        }
        model.addAttribute("totalCartItems", totalCartItems);

        return "web/index";
    }


    @GetMapping("/category/{id}")
    public String getProductsByCategoryId(@PathVariable("id") Long categoryId, Model model) {
        List<Product> products = productService.findProductsByCategoryId(categoryId);
        model.addAttribute("products", products);
        return "web/index :: #tab-content";
    }
    @GetMapping("/new-products")
    public String getNewProducts(Model model) {
        List<Product> newestProducts = productService.findTop8ByOrderByEnteredDateDesc();
        model.addAttribute("newestProducts", newestProducts);
        return "web/index :: #new-products";
    }

    @GetMapping("/top-selling-products")
    public String getTopSellingProducts(Model model) {
        List<Product> topSellingProducts = productService.getTop8BestSellingProducts();
        model.addAttribute("topSellingProducts", topSellingProducts);
        return "web/index :: #top-selling-products";
    }






}
