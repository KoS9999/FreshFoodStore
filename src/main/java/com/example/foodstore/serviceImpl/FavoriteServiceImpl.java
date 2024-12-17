package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
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
    public List<Favorite> getFavoritesByUser(User user) {
        return favoriteRepository.findByUser(user);
    }

    @Override
    public void addFavorite(User user, Product product) {
        if (favoriteRepository.findByUserAndProduct(user, product).isEmpty()) {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);
            favoriteRepository.save(favorite);
            return;
        }
        throw new RuntimeException("Product already in wishlist");
    }

    @Override
    public void removeFavorite(User user, Product product) {
        favoriteRepository.findByUserAndProduct(user, product)
                .ifPresent(favoriteRepository::delete);
    }
}


