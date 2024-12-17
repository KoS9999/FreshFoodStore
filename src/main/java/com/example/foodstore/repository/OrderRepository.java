package com.example.foodstore.repository;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

}
