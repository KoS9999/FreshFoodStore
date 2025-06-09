package com.example.foodstore.dto;

import lombok.Data;

@Data
public class ShippingInfo {
    private String durationText;
    private double distanceKm;
    private double shippingCost;
    private String branchAddress;
    private String branchName;
}
