package com.example.foodstore.service;

import java.util.List;
import java.util.Optional;
import com.example.foodstore.entity.Blog;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService {
    Blog createBlog(Blog blog, MultipartFile imageFile);
    Blog updateBlog(Long id, Blog blog, MultipartFile imageFile);
    void deleteBlog(Long id);
    Optional<Blog> getBlogById(Long id);
    Optional<Blog> getBlogBySlug(String slug);
    List<Blog> getAllBlogs();
}
