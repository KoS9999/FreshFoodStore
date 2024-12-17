package com.example.foodstore.service;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;

import java.util.List;

public interface FavoriteService {
    List<Favorite> getFavoritesByUser(User user);
    void addFavorite(User user, Product product);
    void removeFavorite(User user, Product product);
}
