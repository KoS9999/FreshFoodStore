package com.example.foodstore.dto;

import lombok.Data;

@Data
public class VNPayTransactionDTO {
    private String txnRef;
    private String appUserEmail;
    private String embedData;
    private String items;
    private double amount;
    private String createdAt; // yyyyMMddHHmmss format
}