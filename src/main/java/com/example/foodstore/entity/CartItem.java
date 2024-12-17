package com.example.foodstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double unitPrice;

    private String productImage;

    private int quantity;

    private double totalPrice;

    // Mối quan hệ nhiều CartItem thuộc về một Product
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "productId")
    private Product product;
}

