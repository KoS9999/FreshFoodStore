package com.example.foodstore.service;

import com.example.foodstore.entity.CartItem;

import java.util.Map;

public interface ShoppingCartService {
    void add(CartItem cartItem); // Thêm sản phẩm vào giỏ hàng
    void removeProduct(Long productId); // Xóa sản phẩm khỏi giỏ hàng
    void updateQuantity(Long productId, int quantity); // Cập nhật số lượng sản phẩm trong giỏ hàng
    Map<Long, CartItem> getCartItems(); // Lấy danh sách sản phẩm trong giỏ hàng
    int getCount(); // Lấy tổng số lượng sản phẩm trong giỏ hàng
    double calculateTotal(); // Tính tổng giá trị giỏ hàng

    double getAmount();

}
