package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.service.CartService;
import com.example.foodstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String cartDetail(Model model) {
        model.addAttribute("cart", cartService.getCart());
        return "web/cart-detail";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        Product product = productService.getProductById(productId);
        cartService.addToCart(product, quantity);
        return "Product added to cart!";
    }

    @PostMapping("/update")
    @ResponseBody
    public String updateCart(@RequestParam Long productId, @RequestParam int quantity) {
        Product product = productService.getProductById(productId);
        cartService.updateCart(product, quantity);
        return "Cart updated!";
    }

    @PostMapping("/remove")
    @ResponseBody
    public String removeFromCart(@RequestParam Long productId) {
        Product product = productService.getProductById(productId);
        cartService.removeFromCart(product);
        return "Product removed from cart!";
    }

    @PostMapping("/clear")
    @ResponseBody
    public String clearCart() {
        cartService.clearCart();
        return "Cart cleared!";
    }
}
