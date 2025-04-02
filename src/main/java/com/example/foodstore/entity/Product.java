package com.example.foodstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Column(length = 1000)
    private String description;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date enteredDate;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @ElementCollection
    @CollectionTable(name = "product_seasons", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "month")
    private List<Integer> seasonMonths = new ArrayList<>();

}
