package com.example.foodstore.repository;

import com.example.foodstore.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryId(Long categoryId);

    List<Product> findTop8ByOrderByEnteredDateDesc();

    @Query("SELECT p FROM Product p JOIN OrderDetail od ON p.productId = od.product.productId " +
            "GROUP BY p.productId " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Product> findTop8BestSellingProducts(Pageable pageable);
    Optional<Product> findByProductId(Long id);

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR p.productName LIKE %:keyword%) AND " +
            "(:categoryId = 0 OR p.category.categoryId = :categoryId) AND " +
            "p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProducts(String keyword, Long categoryId, double minPrice, double maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.seasonMonths m WHERE m = :month")
    List<Product> findProductsBySeasonMonth(@Param("month") int month);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

}

