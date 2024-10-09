package com.example.foodstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;

    private int quantity;

    private double price;

    private int discount;

    // Ảnh chính của sản phẩm
    private String productImage;

    private String description;

    @Temporal(TemporalType.DATE)
    private Date enteredDate;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;


    // Liên kết một-nhiều với ProductImage
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    // Các phương thức thêm/xóa ảnh phụ
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
}
