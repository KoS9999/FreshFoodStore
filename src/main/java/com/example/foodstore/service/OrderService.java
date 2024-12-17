package com.example.foodstore.service;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(Long id);

    void save(Order order);

    void updatePaymentStatus(Long orderId, int status);

    void updateOrderStatus(Long orderId, String orderStatus);

    List<Order> findOrdersByUser(User user);

}
