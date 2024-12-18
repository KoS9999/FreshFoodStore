package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductImageService;
import com.example.foodstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void testViewProductPage() throws Exception {
        Mockito.when(productService.findAll()).thenReturn(Collections.singletonList(new Product()));
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    public void testAddProductPage() throws Exception {
        Mockito.when(categoryService.findAll()).thenReturn(Collections.singletonList(new Category()));
        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add-product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    public void testSaveProduct() throws Exception {
        MockMultipartFile mainImageFile = new MockMultipartFile(
                "mainImageFile", "main.jpg", "image/jpeg", new FileInputStream("src/test/resources/sample.jpg"));

        Category category = new Category();
        category.setCategoryId(1L);
        Mockito.when(categoryService.findById(1L)).thenReturn(category);
        Product product = new Product();
        product.setProductId(1L);
        Mockito.when(productService.save(Mockito.any(Product.class))).thenReturn(product);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(mainImageFile)
                        .param("categoryId", "1")
                        .param("productName", "Test Product"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/addImages/1"));
    }

    @Test
    public void testEditProductPage() throws Exception {
        Product product = new Product();
        product.setProductId(1L);

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(categoryService.findAll()).thenReturn(Collections.singletonList(new Category()));

        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-product"))
                .andExpect(model().attribute("product", product))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Mockito.doNothing().when(productService).delete(1L);

        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }
    @Test
    public void testDeleteProductImage() throws Exception {
        Mockito.doNothing().when(productImageService).deleteImageById(1L);

        mockMvc.perform(delete("/admin/products/delete-image/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateProductWithNewMainImage() throws Exception {
        // Mock dữ liệu sản phẩm cũ
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);
        existingProduct.setProductImage("old-image.jpg");

        Category category = new Category();
        category.setCategoryId(1L);

        Mockito.when(productService.findById(1L)).thenReturn(existingProduct);
        Mockito.when(categoryService.findById(1L)).thenReturn(category);

        MockMultipartFile mainImageFile = new MockMultipartFile(
                "mainImageFile", "new-main.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/admin/products/update/1")
                        .file(mainImageFile)
                        .param("productName", "Updated Product")
                        .param("categoryId", "1")
                        .param("price", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        Mockito.verify(productService, Mockito.times(1)).save(Mockito.argThat(updatedProduct ->
                updatedProduct.getProductImage() != null &&
                        updatedProduct.getProductImage().equals("new-main.jpg") &&
                        updatedProduct.getProductName().equals("Updated Product") &&
                        Objects.equals(updatedProduct.getPrice(), 100.0)));
    }

    @Test
    public void testUpdateProductWithoutMainImage() throws Exception {
        // Mock dữ liệu sản phẩm cũ
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);
        existingProduct.setProductImage("old-main.jpg");

        Category category = new Category();
        category.setCategoryId(1L);

        Mockito.when(productService.findById(1L)).thenReturn(existingProduct);
        Mockito.when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(multipart("/admin/products/update/1")
                        .param("productName", "Updated Product Without Main Image")
                        .param("categoryId", "1")
                        .param("price", "150.0")) // Không gửi file ảnh
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        Mockito.verify(productService, Mockito.times(1)).save(Mockito.argThat(updatedProduct ->
                updatedProduct.getProductImage().equals("old-main.jpg") && // Ảnh cũ giữ nguyên
                        updatedProduct.getProductName().equals("Updated Product Without Main Image") &&
                        Objects.equals(updatedProduct.getPrice(), 150.0)));
    }
    @Test
    public void testUpdateProductWithAdditionalImages() throws Exception {
        // Mock dữ liệu sản phẩm cũ
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);

        Category category = new Category();
        category.setCategoryId(1L);

        Mockito.when(productService.findById(1L)).thenReturn(existingProduct);
        Mockito.when(categoryService.findById(1L)).thenReturn(category);

        MockMultipartFile additionalImageFile1 = new MockMultipartFile(
                "additionalImageFiles", "additional1.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile additionalImageFile2 = new MockMultipartFile(
                "additionalImageFiles", "additional2.jpg", "image/jpeg", new byte[]{4, 5, 6});

        mockMvc.perform(multipart("/admin/products/update/1")
                        .file(additionalImageFile1)
                        .file(additionalImageFile2)
                        .param("productName", "Updated Product With Additional Images")
                        .param("categoryId", "1")
                        .param("price", "200.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        Mockito.verify(productService, Mockito.times(1)).save(Mockito.any(Product.class));
        Mockito.verify(productService, Mockito.times(2)).addProductImage(Mockito.any(Product.class), Mockito.anyString());
    }

    @Test
    public void testUpdateProductNotFound() throws Exception {
        Mockito.when(productService.findById(1L)).thenReturn(null); // Không tìm thấy sản phẩm

        mockMvc.perform(multipart("/admin/products/update/1")
                        .param("productName", "Non-existent Product")
                        .param("categoryId", "1")
                        .param("price", "250.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products?error=not_found"));

        Mockito.verify(productService, Mockito.never()).save(Mockito.any(Product.class));
    }
    @Test
    public void testUpdateProductWithInvalidCategory() throws Exception {
        // Mock dữ liệu sản phẩm cũ
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);

        Mockito.when(productService.findById(1L)).thenReturn(existingProduct);
        Mockito.when(categoryService.findById(1L)).thenReturn(null); // Không tìm thấy category

        mockMvc.perform(multipart("/admin/products/update/1")
                        .param("productName", "Updated Product With Invalid Category")
                        .param("categoryId", "1")
                        .param("price", "300.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products?error=invalid_category"));

        Mockito.verify(productService, Mockito.never()).save(Mockito.any(Product.class));
    }



}
