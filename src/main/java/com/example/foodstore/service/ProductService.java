package com.example.foodstore.service;

import com.example.foodstore.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);

    void delete(Long id);

    void update(Long id, Product product);

    Product getProductById(Long id);
    List<Product> getProductsByCategoryId(Long categoryId);
}
