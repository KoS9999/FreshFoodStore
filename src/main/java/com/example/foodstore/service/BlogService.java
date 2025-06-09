package com.example.foodstore.service;

import java.util.List;
import java.util.Optional;
import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService {
    void createBlog(Blog blog, MultipartFile imageFile);
    void updateBlog(Long id, Blog blog, MultipartFile imageFile);
    void deleteBlog(Long id);
    Optional<Blog> getBlogById(Long id);
    Optional<Blog> getBlogBySlug(String slug);
    List<Blog> getAllBlogs();
    List<Blog> getLatestBlogs();
    List<Blog> findByCategory(BlogCategory category);
    Page<Blog> getAllBlogsPage(Pageable pageable);
    Page<Blog> findByCategoryPage(BlogCategory category, Pageable pageable);

}
