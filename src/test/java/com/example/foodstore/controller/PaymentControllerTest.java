package com.example.foodstore.controller;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.repository.OrderRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.EmailService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    // Test createPayment - Success
    @Test
    public void testCreatePaymentSuccess() throws Exception {
        String itemsJson = "[{\"itemid\": 1, \"itemquantity\": 2, \"itemprice\": 50.0}]";
        String embedDataJson = "{\"redirecturl\": \"http://example.com\"}";

        mockMvc.perform(post("/api/payment/createPayment")
                        .param("app_user", "test@example.com")
                        .param("totalPrice", "100.0")
                        .param("items", itemsJson)
                        .param("embedData", embedDataJson)
                        .param("redirect_url", "http://example.com"))
                .andExpect(status().isFound());
    }

    // Test createPayment - Internal Server Error
    @Test
    public void testCreatePaymentError() throws Exception {
        mockMvc.perform(post("/api/payment/createPayment")
                        .param("app_user", "test@example.com")
                        .param("totalPrice", "invalidAmount") // Invalid amount
                        .param("items", "[]")
                        .param("embedData", "{}")
                        .param("redirect_url", "http://example.com"))
                .andExpect(status().isInternalServerError());
    }

    // Test handleCallback - Success
    @Test
    public void testHandleCallbackSuccess() throws Exception {
        String callbackData = "{ \"data\": \"{\\\"app_user\\\":\\\"test@example.com\\\",\\\"amount\\\":100,\\\"embed_data\\\":\\\"{}\\\",\\\"item\\\":\\\"[{\\\\\\\"itemid\\\\\\\":1,\\\\\\\"itemquantity\\\\\\\":1,\\\\\\\"itemprice\\\\\\\":100}]\\\"}\" }";

        User user = new User();
        user.setEmail("test@example.com");
        Product product = new Product();
        product.setProductId(1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/api/payment/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackData))
                .andExpect(status().isOk())
                .andExpect(content().string("Callback handled successfully. Email sent."));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderDetailRepository, times(1)).save(any());
        verify(emailService, times(1)).sendOrderConfirmationEmail(eq("test@example.com"), anyString(), any());
    }

    // Test handleCallback - User Not Found
    @Test
    public void testHandleCallbackUserNotFound() throws Exception {
        String callbackData = "{ \"data\": \"{\\\"app_user\\\":\\\"unknown@example.com\\\",\\\"amount\\\":100,\\\"embed_data\\\":\\\"{}\\\",\\\"item\\\":\\\"[]\\\"}\" }";

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payment/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackData))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    // Test createCODOrder - Success
    @Test
    public void testCreateCODOrderSuccess() throws Exception {
        String itemsJson = "[{\"itemid\": 1, \"itemquantity\": 2, \"itemprice\": 50.0}]";
        String embedDataJson = "{\"address\": \"123 Street\", \"phone\": \"123456789\"}";

        User user = new User();
        user.setEmail("test@example.com");
        Product product = new Product();
        product.setProductId(1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/api/payment/createCODOrder")
                        .param("app_user", "test@example.com")
                        .param("totalPrice", "100.0")
                        .param("items", itemsJson)
                        .param("embedData", embedDataJson))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/order-success"));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderDetailRepository, times(1)).save(any());
        verify(emailService, times(1)).sendOrderConfirmationEmail(eq("test@example.com"), anyString(), any());
    }

    // Test createCODOrder - User Not Found
    @Test
    public void testCreateCODOrderUserNotFound() throws Exception {
        String itemsJson = "[]";
        String embedDataJson = "{}";

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payment/createCODOrder")
                        .param("app_user", "unknown@example.com")
                        .param("totalPrice", "100.0")
                        .param("items", itemsJson)
                        .param("embedData", embedDataJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}
