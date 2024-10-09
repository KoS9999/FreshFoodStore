package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.repository.FavoriteRepository;
import com.example.foodstore.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Override
    public List<Product> getFavoriteProducts() {
        Long userId = 1L; // Giả lập lấy userId người dùng đăng nhập
        return favoriteRepository.findByUserId(userId);
    }

    @Override
    public void addFavorite(Long productId) {
        Long userId = 1L; // Giả lập lấy userId người dùng đăng nhập
        favoriteRepository.addFavorite(userId, productId);
    }

    @Override
    public void removeFavorite(Long productId) {
        Long userId = 1L; // Giả lập lấy userId người dùng đăng nhập
        favoriteRepository.removeFavorite(userId, productId);
    }
}
