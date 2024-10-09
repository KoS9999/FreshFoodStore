package com.example.foodstore.service;

import com.example.foodstore.entity.Product;
import java.util.Map;

public interface CartService {
    Map<Product, Integer> getCart();
    void addToCart(Product product, int quantity);
    void updateCart(Product product, int quantity);
    void removeFromCart(Product product);
    void clearCart();
}
