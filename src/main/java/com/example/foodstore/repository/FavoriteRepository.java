package com.example.foodstore.repository;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Product> findByUserId(Long userId);

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);
}
