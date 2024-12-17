package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductImageServiceImpl {
    @Autowired
    private ProductImageRepository productImageRepository;

    public ProductImage save(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }
}
