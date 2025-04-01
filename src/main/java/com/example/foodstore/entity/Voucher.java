package com.example.foodstore.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voucherid;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean status; // true = ACTIVE
    private double discountPercent;
    private LocalDateTime createdAt = LocalDateTime.now();
}
