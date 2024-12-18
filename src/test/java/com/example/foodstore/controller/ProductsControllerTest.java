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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProductsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductsController productsController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productsController).build();
    }

    @Test
    public void testShowShopPage() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Test Product");

        Category category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Test Category");

        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

        when(productService.searchProducts(anyString(), anyLong(), anyDouble(), anyDouble(), any(PageRequest.class)))
                .thenReturn(productPage);
        when(categoryService.findAll()).thenReturn(Collections.singletonList(category));

        mockMvc.perform(get("/shop")
                        .param("keyword", "")
                        .param("categoryId", "0")
                        .param("minPrice", "0")
                        .param("maxPrice", "1000000")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/shop"))
                .andExpect(model().attributeExists("products", "currentPage", "totalPages", "categories", "keyword", "categoryId", "minPrice", "maxPrice"));
    }
}
