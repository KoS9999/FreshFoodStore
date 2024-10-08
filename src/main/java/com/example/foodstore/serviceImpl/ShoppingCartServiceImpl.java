package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.CartItem;
import com.example.foodstore.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    public void addProductToCart(Product product, int quantity) {
        CartItem existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem == null) {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.setTotalPrice(product.getPrice() * quantity);
            cartItems.add(newItem);
        } else {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setTotalPrice(existingItem.getUnitPrice() * existingItem.getQuantity());
        }
    }

    @Override
    public void removeProductFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getProductId().equals(productId));
    }

    @Override
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    @Override
    public double calculateTotal() {
        return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}

