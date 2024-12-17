package com.example.foodstore.controller;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
import com.example.foodstore.service.FavoriteService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String getWishlist(Model model) {
        User user = userService.getLoggedInUser();
        List<Favorite> favorites = favoriteService.getFavoritesByUser(user);
        model.addAttribute("favorites", favorites);
        return "web/wishlist";
    }
    @PostMapping("/add")
    @ResponseBody
    public String addToWishlist(@RequestParam Long productId) {
        User user = userService.getLoggedInUser();
        Product product = productService.getProductById(productId);
        favoriteService.addFavorite(user, product);
        return "Product added to wishlist";
    }

    @DeleteMapping("/remove")
    @ResponseBody
    public ResponseEntity<String> removeFromWishlist(@RequestParam Long productId) {
        User user = userService.getLoggedInUser(); // Lấy thông tin người dùng
        Product product = productService.getProductById(productId); // Tìm sản phẩm theo ID
        favoriteService.removeFavorite(user, product); // Gọi Service để xóa sản phẩm
        return ResponseEntity.ok("Product removed from wishlist."); // Trả về phản hồi thành công
    }



}


