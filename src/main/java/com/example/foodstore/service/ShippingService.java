package com.example.foodstore.service;

import com.example.foodstore.dto.ShippingInfo;

public interface ShippingService {
    ShippingInfo calculateShipping(String destinationAddress);
}