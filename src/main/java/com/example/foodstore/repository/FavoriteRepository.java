package com.example.foodstore.repository;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndProduct(User user, Product product);
}

