package com.example.foodstore.service;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;

import java.util.List;

public interface ProductImageService {
    ProductImage save(ProductImage additionalImage);

    List<ProductImage> findByProduct(Product product);

    void deleteImageById(Long imageId);


}
