package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Category;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String viewCategoryPage(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/category";
    }
    @GetMapping("/add")
    public String addCategoryPage(Model model) {
        model.addAttribute("category", new Category());
        return "admin/add-category";
    }
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               @RequestParam("categoryImageFile") MultipartFile categoryImageFile) throws IOException {

        String uploadDir = new File("src/main/resources/static/admin/img/category").getAbsolutePath();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = categoryImageFile.getOriginalFilename();
        FileUploadUtil.saveFile(uploadDir, fileName, categoryImageFile);
        category.setCategoryImage(fileName);
        categoryService.save(category);
        return "redirect:/admin/categories";
    }
    @GetMapping("/edit/{id}")
    public String editCategoryPage(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);
        if (category != null) {
            model.addAttribute("category", category);
            return "admin/edit-category";
        }
        return "redirect:/admin/categories";
    }
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute("category") Category category,
                                 @RequestParam("categoryImageFile") MultipartFile categoryImageFile) throws IOException {
        Category existingCategory = categoryService.findById(id);
        if (existingCategory == null) {
            return "redirect:/admin/categories";
        }
        if (categoryImageFile.isEmpty()) {
            category.setCategoryImage(existingCategory.getCategoryImage());
        } else {
            // Nếu người dùng chọn ảnh mới, lưu ảnh
            String fileName = categoryImageFile.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/admin/img/category/";
            FileUploadUtil.saveFile(uploadDir, fileName, categoryImageFile);
            category.setCategoryImage(fileName);
        }
        categoryService.save(category);
        return "redirect:/admin/categories";
    }
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
