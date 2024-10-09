package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Order;
import com.example.foodstore.repository.OrderRepository;
import com.example.foodstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(0); // Giả sử 0 đại diện cho "Cancelled"
            orderRepository.save(order);
        }

    }

    @Override
    public void confirmOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(1); // Giả sử 1 đại diện cho "Confirmed"
            orderRepository.save(order);
        }

    }

    @Override
    public void markAsDelivered(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(2); // Giả sử 2 đại diện cho "Delivered"
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> getOrderHistory() {
        // Giả sử lấy lịch sử mua hàng cho người dùng đã đăng nhập
        Long userId = 1L; // Thay bằng thông tin người dùng đăng nhập
        return orderRepository.findByUser_UserId(userId);
    }

    @Override
    public void processCheckout() {
        // Logic xử lý thanh toán
        // Lưu thông tin đơn hàng vào database
    }

    @Override
    public void sendOrderConfirmationEmail() {
        // Logic gửi email xác nhận đơn hàng
    }

}



