package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Category;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.util.FileUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    // Test view Category page
    @Test
    public void testViewCategoryPage() throws Exception {
        Mockito.when(categoryService.findAll()).thenReturn(java.util.Collections.singletonList(new Category()));
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category"))
                .andExpect(model().attributeExists("categories"));
    }

    // Test add category page
    @Test
    public void testAddCategoryPage() throws Exception {
        mockMvc.perform(get("/admin/categories/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add-category"))
                .andExpect(model().attributeExists("category"));
    }

    // Test save category
    @Test
    public void testSaveCategory() throws Exception {
        MockMultipartFile categoryImageFile = new MockMultipartFile(
                "categoryImageFile", "test.jpg", "image/jpeg", "some image content".getBytes());

        Category category = new Category();
        category.setCategoryImage("test.jpg");

        Mockito.when(categoryService.save(Mockito.any(Category.class))).thenReturn(category);

        mockMvc.perform(multipart("/admin/categories/save")
                        .file(categoryImageFile)
                        .param("categoryName", "Test Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    // Test update category with new image
    @Test
    public void testUpdateCategoryWithNewImage() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setCategoryId(1L);
        existingCategory.setCategoryImage("old-image.jpg");
        Mockito.when(categoryService.findById(1L)).thenReturn(existingCategory);
        MockMultipartFile categoryImageFile = new MockMultipartFile(
                "categoryImageFile", "new-image.jpg", "image/jpeg", "new image content".getBytes());

        Mockito.when(categoryService.save(Mockito.any(Category.class))).thenReturn(existingCategory);
        mockMvc.perform(multipart("/admin/categories/update/1")
                        .file(categoryImageFile)
                        .param("categoryName", "Updated Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
        Mockito.verify(categoryService, Mockito.times(1)).save(Mockito.any(Category.class));
    }

    // Test update category without new image
    @Test
    public void testUpdateCategoryWithoutNewImage() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setCategoryId(1L);
        existingCategory.setCategoryImage("old-image.jpg");
        existingCategory.setCategoryName("Old Category");

        Mockito.when(categoryService.findById(1L)).thenReturn(existingCategory);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/update/1")
                        .file("categoryImageFile", new byte[0])
                        .param("categoryName", "Updated Category"))  // Gửi ảnh trống
                .andExpect(status().is3xxRedirection())  // Kiểm tra mã trạng thái chuyển hướng
                .andExpect(redirectedUrl("/admin/categories"));  // Kiểm tra URL chuyển hướng đúng

        Mockito.verify(categoryService, Mockito.times(1)).save(Mockito.argThat(updatedCategory ->
                updatedCategory.getCategoryImage().equals("old-image.jpg") &&  // Đảm bảo ảnh không thay đổi
                        updatedCategory.getCategoryName().equals("Updated Category")));  // Kiểm tra tên danh mục đã được cập nhật
    }


    @Test
    public void testEditCategoryPage() throws Exception {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setCategoryImage("test.jpg");

        Mockito.when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/admin/categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-category"))
                .andExpect(model().attribute("category", category));
    }
    @Test
    public void testDeleteCategory() throws Exception {
        Mockito.doNothing().when(categoryService).delete(1L);

        mockMvc.perform(get("/admin/categories/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }
}
