package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.User;
import com.example.foodstore.service.FavoriteService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
public class FavoriteControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private UserService userService;

    @MockBean
    private ProductService productService;

    @Test
    public void testGetWishlist() throws Exception {
        User user = new User();
        Mockito.when(userService.getLoggedInUser()).thenReturn(user);

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(get("/wishlist").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("web/wishlist"))
                .andExpect(model().attributeExists("favorites"));
    }

    @Test
    public void testAddToWishlist() throws Exception {
        User user = new User();
        Product product = new Product();
        Mockito.when(userService.getLoggedInUser()).thenReturn(user);
        Mockito.when(productService.getProductById(1L)).thenReturn(product);

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(post("/wishlist/add")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product added to wishlist"));
    }

    @Test
    public void testRemoveFromWishlist() throws Exception {
        User user = new User();
        Product product = new Product();
        Mockito.when(userService.getLoggedInUser()).thenReturn(user);
        Mockito.when(productService.getProductById(1L)).thenReturn(product);

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(delete("/wishlist/remove")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product removed from wishlist."));
    }
}
