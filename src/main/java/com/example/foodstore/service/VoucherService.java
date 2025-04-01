package com.example.foodstore.service;

import com.example.foodstore.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> findAll();
    Optional<Voucher> findById(Long id);
    Voucher save(Voucher voucher);
    void deleteById(Long id);
}
