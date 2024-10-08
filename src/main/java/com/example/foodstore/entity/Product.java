package com.example.foodstore.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

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

    private String productImage;

    private String description;

    @Temporal(TemporalType.DATE)
    private Date enteredDate;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
}

