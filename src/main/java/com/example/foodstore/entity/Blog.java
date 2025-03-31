package com.example.foodstore.entity;

import com.example.foodstore.entity.BlogStatus;
import com.example.foodstore.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "blog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String markdownContent;

    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "blog_product",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> relatedProducts;

}
