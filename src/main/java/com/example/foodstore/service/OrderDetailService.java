package com.example.foodstore.service;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> findDetailsByOrder(Order order);
}

