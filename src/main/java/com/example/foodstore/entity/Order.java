package com.example.foodstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Temporal(TemporalType.DATE)
    private Date orderDate;

    private String voucherCode;

    private Double amount;

    private String address;

    private String phone;

    private String note;

    @Column(name = "duration_text")
    private String durationText;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "voucher_discount")
    private Double voucherDiscount;

    @Column(name = "redeem_amount")
    private Double redeemAmount;

    private int status;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "used_points")
    private Integer usedPoints;

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

