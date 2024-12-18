package com.example.foodstore.controller;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private HomeController homeController;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        session = new MockHttpSession();
    }

    @Test
    public void testIndex() throws Exception {
        Category category = new Category();
        category.setCategoryId(1L);
        Product product = new Product();

        when(categoryService.findAll()).thenReturn(Collections.singletonList(category));
        when(productService.findProductsByCategoryId(1L)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("web/index"))
                .andExpect(model().attributeExists("categories", "products", "totalCartItems", "keyword"));
    }

    @Test
    public void testGetProductsByCategoryId() throws Exception {
        Product product = new Product();

        when(productService.findProductsByCategoryId(1L)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/category/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/index :: #tab-content"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    public void testGetNewProducts() throws Exception {
        when(productService.findTop8ByOrderByEnteredDateDesc()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/new-products"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/index :: #new-products"))
                .andExpect(model().attributeExists("newestProducts"));
    }

    @Test
    public void testGetTopSellingProducts() throws Exception {
        when(productService.getTop8BestSellingProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/top-selling-products"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/index :: #top-selling-products"))
                .andExpect(model().attributeExists("topSellingProducts"));
    }
}
