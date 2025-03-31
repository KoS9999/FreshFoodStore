package com.example.foodstore.repository;

import com.example.foodstore.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlug(String slug);

    List<Blog> findByMarkdownContentContainingIgnoreCase(String keyword);

}