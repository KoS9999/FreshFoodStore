package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.CartItem;
import com.example.foodstore.entity.Product;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final String CART_SESSION_KEY = "cartItems";

    @Autowired
    private HttpSession session;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void add(CartItem cartItem) {
        Map<Long, CartItem> cartItems = getCartItems();
        Long productId = cartItem.getProduct().getProductId();
        CartItem item = cartItems.get(productId);
        if (item == null) {
            cartItem.setTotalPrice(cartItem.getUnitPrice() * cartItem.getQuantity());
            cartItems.put(productId, cartItem);
        } else {
            item.setQuantity(item.getQuantity() + cartItem.getQuantity());
            item.setTotalPrice(item.getUnitPrice() * item.getQuantity());
        }
        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    @Override
    public void updateQuantity(Long productId, int quantity) {
        Map<Long, CartItem> cartItems = getCartItems();
        CartItem item = cartItems.get(productId);
        if (item != null) {
            if (quantity > 0) {
                item.setQuantity(quantity);
                item.setTotalPrice(item.getUnitPrice() * quantity);
            } else {
                cartItems.remove(productId);
            }
            session.setAttribute(CART_SESSION_KEY, cartItems);
        } else {
            System.out.println("Product ID not found in cart for updating quantity: " + productId);
        }
    }

    @Override
    public void removeProduct(Long productId) {
        Map<Long, CartItem> cartItems = getCartItems();
        if (cartItems.containsKey(productId)) {
            cartItems.remove(productId);
            session.setAttribute(CART_SESSION_KEY, cartItems);
        } else {
            System.out.println("Product ID not found in cart for removal: " + productId);
        }
    }


    @Override
    public Map<Long, CartItem> getCartItems() {
        Map<Long, CartItem> cartItems = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cartItems == null) {
            cartItems = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cartItems);
        }
        return cartItems;
    }

    @Override
    public int getCount() {
        return getCartItems().values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public double calculateTotal() {
        return getCartItems().values().stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    @Override
    public double getAmount() {
        return calculateTotal();
    }
}
