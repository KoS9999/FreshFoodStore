package com.example.foodstore.service;

import com.example.foodstore.entity.CartItem;

import java.util.Map;

public interface ShoppingCartService {
    void add(CartItem cartItem);
    void removeProduct(Long productId);
    void updateQuantity(Long productId, int quantity);
    Map<Long, CartItem> getCartItems();
    int getCount();
    double calculateTotal();
    double getAmount();

}
