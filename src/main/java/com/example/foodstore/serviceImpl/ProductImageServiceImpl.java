package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.repository.ProductImageRepository;
import com.example.foodstore.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageServiceImpl implements ProductImageService {
    @Autowired
    private ProductImageRepository productImageRepository;

    @Override
    public ProductImage save(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }

    @Override
    public List<ProductImage> findByProduct(Product product) {
        return productImageRepository.findByProduct(product);
    }


    @Override
    public void deleteImageById(Long imageId) {
        productImageRepository.deleteById(imageId);
    }
}
