package com.example.foodstore.service;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);

    void update(Long id, Category category);

    void delete(Long id);

    List<Product> getAll();
}

