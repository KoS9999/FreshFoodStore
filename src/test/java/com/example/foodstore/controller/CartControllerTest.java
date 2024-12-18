package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.service.ShoppingCartService;
import com.example.foodstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartController cartController;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        session = new MockHttpSession();
    }

    @Test
    public void testViewCart() throws Exception {
        when(shoppingCartService.getCartItems()).thenReturn(Collections.emptyMap());
        when(shoppingCartService.calculateTotal()).thenReturn(0.0);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/view"))
                .andExpect(model().attributeExists("cartItems", "total"));
    }

    @Test
    public void testAddToCart() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Test Product");
        product.setPrice(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/cart/add")
                        .param("productId", "1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.totalCartItems").exists());
    }

    @Test
    public void testUpdateCartQuantity() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .param("productId", "1")
                        .param("quantity", "3")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(shoppingCartService, times(1)).updateQuantity(1L, 3);
    }

    @Test
    public void testRemoveProductFromCart() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .param("productId", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(shoppingCartService, times(1)).removeProduct(1L);
    }
}
