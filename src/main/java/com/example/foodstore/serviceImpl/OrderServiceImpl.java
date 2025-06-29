package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.OrderRepository;
import com.example.foodstore.service.OrderService;
import com.example.foodstore.entity.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByOrderIdDesc();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Override
    public void updatePaymentStatus(Long orderId, int status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setOrderStatus(OrderStatus.valueOf(orderStatus));
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (order.getOrderStatus() == OrderStatus.PENDING && order.getStatus() == 0) {
                order.setOrderStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
            } else {
                throw new IllegalStateException("Đơn hàng không thể hủy vì không ở trạng thái PENDING hoặc đã thanh toán");
            }
        } else {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId);
        }
    }

    @Override
    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

}
