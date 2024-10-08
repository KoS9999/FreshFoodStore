package com.example.foodstore.service;

import com.example.foodstore.entity.Order;
import java.util.List;

public interface OrderService {
    List<Order> findAll();
    Order findById(Long id);
    void cancelOrder(Long id);
    void confirmOrder(Long id);
    void markAsDelivered(Long id);
    
}
