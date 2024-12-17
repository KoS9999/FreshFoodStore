package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.service.ProductImageService;
import com.example.foodstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/product-details")
public class ProductDetailController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findByProductId(id);
        List<ProductImage> productImages = productImageService.findByProduct(product);
        model.addAttribute("product", product);
        model.addAttribute("productImages", productImages);
        return "web/product-details";
    }
}
