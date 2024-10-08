package com.example.foodstore.service;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.CartItem;

import java.util.List;

public interface ShoppingCartService {
    void addProductToCart(Product product, int quantity);

    void removeProductFromCart(Long productId);

    List<CartItem> getCartItems();

    double calculateTotal();
}

