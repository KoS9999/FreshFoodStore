package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

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
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") Product product) {
        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, @ModelAttribute("product") Product product) {
        productService.update(id, product);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}

