package com.example.foodstore.service;

import com.example.foodstore.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);

    void update(Long id, Category category);

    void delete(Long id);

    void deleteById(Long id);
}

