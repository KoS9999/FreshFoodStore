package com.example.foodstore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    @JsonIgnore
    private OrderDetail orderDetail;



    private int rating;
    private String reviewText;
    private Date reviewDate;

    @Column(nullable = true)
    private String responseText;

    @Column(nullable = true)
    private String imageUrl1;

    @Column(nullable = true)
    private String imageUrl2;

    @Column(nullable = false)
    private Boolean visible = false;

    public Review(User user, Product product, OrderDetail orderDetail, int rating, String reviewText, Date reviewDate, String responseText, String imageUrl1, String imageUrl2) {
        this.user = user;
        this.product = product;
        this.orderDetail = orderDetail;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
        this.responseText = responseText;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
    }
    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", rating=" + rating +
                ", reviewText='" + reviewText + '\'' +
                '}';
    }

}
