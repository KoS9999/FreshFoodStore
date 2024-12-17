package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.CartItem;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", shoppingCartService.getCartItems());
        model.addAttribute("total", shoppingCartService.calculateTotal());
        return "cart/view";
    }
    @GetMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Long productId, HttpSession session) {
        Product product = productRepository.findById(productId).orElse(null);
        Map<String, Object> response = new HashMap<>();

        if (product != null) {
            CartItem item = new CartItem();
            item.setId(productId);
            item.setProduct(product);
            item.setName(product.getProductName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(1);
            item.setTotalPrice(item.getUnitPrice() * item.getQuantity());
            item.setProductImage(product.getProductImage());
            shoppingCartService.add(item);
            session.setAttribute("cartItems", shoppingCartService.getCartItems());
            session.setAttribute("totalCartItems", shoppingCartService.getCount());

            response.put("status", "success");
            response.put("totalCartItems", shoppingCartService.getCount());
            response.put("cartItems", new ArrayList<>(shoppingCartService.getCartItems().values()));
            response.put("totalPrice", shoppingCartService.calculateTotal());
        } else {
            response.put("status", "error");
            response.put("message", "Product not found");
        }

        return ResponseEntity.ok(response);
    }
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartQuantity(@RequestParam Long productId, @RequestParam int quantity, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            shoppingCartService.updateQuantity(productId, quantity);
            session.setAttribute("cartItems", shoppingCartService.getCartItems());
            session.setAttribute("totalCartItems", shoppingCartService.getCount());
            response.put("status", "success");
            response.put("totalCartItems", shoppingCartService.getCount());
            response.put("cartItems", new ArrayList<>(shoppingCartService.getCartItems().values()));
            response.put("totalPrice", shoppingCartService.calculateTotal());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Could not update product quantity.");
            e.printStackTrace();
        }
        return ResponseEntity.ok(response);
    }
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeProductFromCart(@RequestParam Long productId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            shoppingCartService.removeProduct(productId);
            session.setAttribute("cartItems", shoppingCartService.getCartItems());
            session.setAttribute("totalCartItems", shoppingCartService.getCount());
            response.put("status", "success");
            response.put("totalCartItems", shoppingCartService.getCount());
            response.put("cartItems", new ArrayList<>(shoppingCartService.getCartItems().values()));
            response.put("totalPrice", shoppingCartService.calculateTotal());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Could not remove product from cart.");
            e.printStackTrace();
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String appUser = authentication.getName();
        Map<Long, CartItem> cartItems = shoppingCartService.getCartItems();
        double totalPrice = shoppingCartService.calculateTotal();
        double shippingCost = 0.00;
        double vat = 0.00;
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("itename", cartItem.getName());
            item.put("itemprice", (long) cartItem.getUnitPrice());
            item.put("itemquantity", cartItem.getQuantity());
            items.add(item);
        }
        model.addAttribute("user", appUser);
        model.addAttribute("cartItems", cartItems.values());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("vat", vat);
        model.addAttribute("items", items);
        return "web/checkout";
    }

}
