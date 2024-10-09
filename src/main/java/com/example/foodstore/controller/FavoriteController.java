package com.example.foodstore.controller;

import com.example.foodstore.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public String favoriteList(Model model) {
        model.addAttribute("favorites", favoriteService.getFavoriteProducts());  // Lấy danh sách yêu thích
        return "web/favorite";
    }

    @PostMapping("/doFavorite")
    @ResponseBody
    public String doFavorite(@RequestParam Long productId) {
        favoriteService.addFavorite(productId);  // Thêm sản phẩm vào danh sách yêu thích
        return "Added to favorites!";
    }

    @PostMapping("/undoFavorite")
    @ResponseBody
    public String undoFavorite(@RequestParam Long productId) {
        favoriteService.removeFavorite(productId);  // Xóa sản phẩm khỏi danh sách yêu thích
        return "Removed from favorites!";
    }
}
