package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.repository.ProductImageRepository;
import com.example.foodstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override

    public Product save(Product product) {
        return productRepository.save(product);
    }


    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public void addProductImage(Product product, String imageUrl) {
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(imageUrl);
        productImage.setProduct(product);
        productImageRepository.save(productImage);
    }

    @Override
    public void deleteProductImages(Product product) {
        List<ProductImage> images = productImageRepository.findByProduct(product);
        productImageRepository.deleteAll(images);
    }

    @Override
    public List<Product> findProductsByCategoryId(Long id) {
        return productRepository.findByCategory_CategoryId(id);
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    @Override
    public List<Product> findTop8ByOrderByEnteredDateDesc() {
        return productRepository.findTop8ByOrderByEnteredDateDesc();
    }

    @Override
    public List<Product> getTop8BestSellingProducts() {
        Pageable pageable = PageRequest.of(0, 8);
        return productRepository.findTop8BestSellingProducts(pageable);
    }

    @Override
    public Product findByProductId(Long id) {
        return productRepository.findByProductId(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public Page<Product> searchProducts(String keyword, Long categoryId, double minPrice, double maxPrice, Pageable pageable) {
        return productRepository.findProducts(keyword, categoryId, minPrice, maxPrice, pageable);
    }

}


