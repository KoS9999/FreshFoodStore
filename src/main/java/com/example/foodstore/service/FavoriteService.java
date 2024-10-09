package com.example.foodstore.service;

import java.util.List;
import com.example.foodstore.entity.Product;

public interface FavoriteService {
    List<Product> getFavoriteProducts();
    void addFavorite(Long productId);
    void removeFavorite(Long productId);
}
