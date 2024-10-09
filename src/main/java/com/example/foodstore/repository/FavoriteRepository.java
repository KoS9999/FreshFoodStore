package com.example.foodstore.repository;

import com.example.foodstore.entity.Favorite;
import com.example.foodstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Truy vấn để tìm sản phẩm yêu thích của người dùng dựa trên userId
    @Query("SELECT f.product FROM Favorite f WHERE f.user.userId = ?1")
    List<Product> findByUser_UserId(Long userId);

    // Phương thức thêm sản phẩm vào danh sách yêu thích
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO favorites (user_id, product_id) VALUES (?1, ?2)", nativeQuery = true)
    void addFavorite(Long userId, Long productId);

    // Phương thức xóa sản phẩm khỏi danh sách yêu thích
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM favorites WHERE user_id = ?1 AND product_id = ?2", nativeQuery = true)
    void removeFavorite(Long userId, Long productId);
}
