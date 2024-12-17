package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetail> findDetailsByOrder(Order order) {
        return orderDetailRepository.findByOrder(order);
    }
}

