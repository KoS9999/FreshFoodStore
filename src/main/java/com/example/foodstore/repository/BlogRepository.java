package com.example.foodstore.repository;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlug(String slug);
    List<Blog> findByMarkdownContentContainingIgnoreCase(String keyword);
    List<Blog> findByCategory(BlogCategory category);
    List<Blog> findAllByOrderByCreatedAtDesc();
    Page<Blog> findByCategory(BlogCategory category, Pageable pageable);

}