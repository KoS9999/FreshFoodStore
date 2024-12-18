package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.OrderStatus;
import com.example.foodstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class OrderControllerTest {


    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }
    @Test
    public void testViewOrderList() throws Exception {
        // Giả lập dữ liệu đơn hàng
        Order order = new Order();
        order.setOrderId(1L);
        order.setStatus(1);
        when(orderService.findAll()).thenReturn(Collections.singletonList(order));
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order-list"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("statuses"));
        verify(orderService, times(1)).findAll();
    }
    @Test
    public void testUpdateOrderStatus() throws Exception {
        Long orderId = 1L;
        String newStatus = "SHIPPING";
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderStatus(OrderStatus.PENDING);
        when(orderService.findById(orderId)).thenReturn(Optional.of(order));
        mockMvc.perform(get("/admin/orders/update-order-status/{id}/{status}", orderId, newStatus))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));

        verify(orderService, times(1)).save(order);
        verify(orderService, times(1)).findById(orderId);
    }

    // Test for updateOrderPaymentStatus (with status as int)
    @Test
    public void testUpdateOrderPaymentStatus() throws Exception {
        Long orderId = 1L;
        int newStatus = 2;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(1);

        when(orderService.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/admin/orders/update-status/{id}/{status}", orderId, newStatus))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
        verify(orderService, times(1)).save(order);
        verify(orderService, times(1)).findById(orderId);
    }

    // Test for viewOrderDetails
    @Test
    public void testViewOrderDetails() throws Exception {
        Long orderId = 1L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(1);

        List<OrderDetail> orderDetails = Arrays.asList(new OrderDetail(), new OrderDetail());
        when(orderService.findById(orderId)).thenReturn(Optional.of(order));
        order.setOrderDetails(orderDetails);  // Đảm bảo rằng orderDetails được gán vào order

        mockMvc.perform(get("/admin/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order-details"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("orderDetails"));
        verify(orderService, times(1)).findById(orderId);
    }

    @Test
    public void testViewOrderDetailsNotFound() throws Exception {
        Long orderId = 1L;

        when(orderService.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/orders/{id}", orderId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders?error=OrderNotFound"));
        verify(orderService, times(1)).findById(orderId);
    }
}
