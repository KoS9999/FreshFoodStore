package com.example.foodstore.service;

import com.example.foodstore.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void delete(Long id);
    void addProductImage(Product product, String imageUrl);
    void deleteProductImages(Product product);
    List<Product> findProductsByCategoryId(Long id);
    Product getProductById(Long productId);

    List<Product> findTop8ByOrderByEnteredDateDesc();

    List<Product> getTop8BestSellingProducts();

    Product findByProductId(Long id);
    Page<Product> searchProducts(String keyword, Long categoryId, double minPrice, double maxPrice, Pageable pageable);


}
