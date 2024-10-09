package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.service.CartService;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private final HttpSession session;

    public CartServiceImpl(HttpSession session) {
        this.session = session;
    }

    @Override
    public Map<Product, Integer> getCart() {
        Map<Product, Integer> cart = (Map<Product, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @Override
    public void addToCart(Product product, int quantity) {
        Map<Product, Integer> cart = getCart();
        cart.put(product, cart.getOrDefault(product, 0) + quantity);
        session.setAttribute("cart", cart);
    }

    @Override
    public void updateCart(Product product, int quantity) {
        Map<Product, Integer> cart = getCart();
        if (cart.containsKey(product)) {
            cart.put(product, quantity);
        }
        session.setAttribute("cart", cart);
    }

    @Override
    public void removeFromCart(Product product) {
        Map<Product, Integer> cart = getCart();
        cart.remove(product);
        session.setAttribute("cart", cart);
    }

    @Override
    public void clearCart() {
        session.setAttribute("cart", new HashMap<>());
    }
}
