package com.example.foodstore.controller;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.service.BlogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class NewsController {
    private final BlogService blogService;

    public NewsController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/news")
    public String showNewsGrid(Model model) {
        List<Blog> blogs = blogService.getAllBlogs();
        model.addAttribute("blogs", blogs);
        return "web/blog-grid";
    }

    @GetMapping("/news/{slug}")
    public String showNewsDetail(@PathVariable String slug, Model model) {
        Optional<Blog> blog = blogService.getBlogBySlug(slug);
        if (blog.isPresent()) {
            model.addAttribute("blog", blog.get());
            return "web/blog-details";
        }
        return "redirect:/news";
    }

}
