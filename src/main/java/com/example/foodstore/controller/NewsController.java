package com.example.foodstore.controller;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.BlogCategory;
import com.example.foodstore.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Optional;

@Controller
public class NewsController {
    private final BlogService blogService;

    public NewsController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/news")
    public String showNewsGrid(Model model,
                               @RequestParam(value = "category", required = false) String category,
                               @PageableDefault(size = 6) Pageable pageable) {
        Page<Blog> blogPage;
        if (category != null && !category.isEmpty()) {
            try {
                BlogCategory blogCategory = BlogCategory.valueOf(category.toUpperCase());
                blogPage = blogService.findByCategoryPage(blogCategory, pageable);
                model.addAttribute("currentCategory", blogCategory.getDisplayName());
            } catch (IllegalArgumentException e) {
                blogPage = blogService.getAllBlogsPage(pageable);
            }
        } else {
            blogPage = blogService.getAllBlogsPage(pageable);
        }
        model.addAttribute("blogs", blogPage.getContent());
        model.addAttribute("page", blogPage);
        model.addAttribute("categories", Arrays.asList(BlogCategory.values()));
        return "web/blog-grid";
    }

    @GetMapping("/news/{slug}")
    public String showNewsDetail(@PathVariable String slug, Model model) {
        Optional<Blog> blog = blogService.getBlogBySlug(slug);
        if (blog.isPresent()) {
            model.addAttribute("blog", blog.get());
            model.addAttribute("categories", Arrays.asList(BlogCategory.values()));
            return "web/blog-details";
        }
        return "redirect:/news";
    }
}