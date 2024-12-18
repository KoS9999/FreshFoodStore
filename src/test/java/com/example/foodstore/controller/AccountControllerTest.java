package com.example.foodstore.controller;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.User;
import com.example.foodstore.service.OrderDetailService;
import com.example.foodstore.service.OrderService;
import com.example.foodstore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDetailService orderDetailService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    public void testViewAccount() throws Exception {
        User user = new User();
        user.setName("Test User");
        Order order = new Order();
        order.setOrderId(1L);

        when(userService.getLoggedInUser()).thenReturn(user);
        when(orderService.findOrdersByUser(user)).thenReturn(Collections.singletonList(order));
        when(orderDetailService.findDetailsByOrder(order)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/account"))
                .andExpect(model().attributeExists("user", "orders", "orderDetailsMap"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        User currentUser = new User();
        currentUser.setName("Old Name");

        User updatedUser = new User();
        updatedUser.setName("New Name");

        when(userService.getLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/account/update")
                        .param("name", updatedUser.getName())
                        .param("phone", "123456789")
                        .param("address", "Test Address"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));

        verify(userService, times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testGetOrderDetails() throws Exception {
        Order order = new Order();
        order.setOrderId(1L);
        List<OrderDetail> orderDetails = Collections.emptyList();

        when(orderService.findById(1L)).thenReturn(Optional.of(order));
        when(orderDetailService.findDetailsByOrder(order)).thenReturn(orderDetails);

        mockMvc.perform(get("/account/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
