package com.example.foodstore.controller;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.service.ProductImageService;
import com.example.foodstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProductDetailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductDetailController productDetailController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productDetailController).build();
    }

    @Test
    public void testProductDetail() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        ProductImage productImage = new ProductImage();

        when(productService.findByProductId(1L)).thenReturn(product);
        when(productImageService.findByProduct(product)).thenReturn(Collections.singletonList(productImage));

        mockMvc.perform(get("/product-details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/product-details"))
                .andExpect(model().attributeExists("product", "productImages"));
    }
}
