package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.service.BlogService;
import com.example.foodstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/blogs")
public class BlogController {
    private final BlogService blogService;

    @Autowired
    ProductService productService;


    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public String getAllBlogs(Model model) {
        List<Blog> blogs = blogService.getAllBlogs();
        model.addAttribute("blogs", blogs);
        return "admin/blog-list";
    }

    @GetMapping("/create")
    public String createBlogForm(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("products", productService.getAllProducts());
        return "admin/add-blog";
    }

    @PostMapping("/save")
    public String saveBlog(@ModelAttribute Blog blog,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           @RequestParam(value = "relatedProducts", required = false) List<Long> relatedProductIds) {
        if (relatedProductIds != null) {
            blog.setRelatedProducts(productService.getProductsByIds(relatedProductIds));
        }
        blogService.createBlog(blog, imageFile);
        return "redirect:/admin/blogs";
    }

    @GetMapping("/edit/{id}")
    public String editBlog(@PathVariable Long id, Model model) {
        Blog blog = blogService.getBlogById(id).orElse(new Blog());
        model.addAttribute("blog", blog);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("relatedProducts", blog.getRelatedProducts());
        return "admin/edit-blog";
    }

    @PostMapping("/update/{id}")
    public String updateBlog(@PathVariable Long id,
                             @ModelAttribute Blog blog,
                             @RequestParam(value = "relatedProducts", required = false) List<Long> relatedProductIds,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (relatedProductIds != null) {
            blog.setRelatedProducts(productService.getProductsByIds(relatedProductIds));
        } else {
            blog.setRelatedProducts(new ArrayList<>());
        }
        blogService.updateBlog(id, blog, imageFile);
        return "redirect:/admin/blogs";
    }

    @GetMapping("/delete/{id}")
    public String deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return "redirect:/admin/blogs";
    }
}
