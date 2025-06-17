package com.example.foodstore.controller;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.foodstore.entity.Product;
import com.example.foodstore.repository.ProductRepository;

import java.io.ByteArrayOutputStream;

@RestController
public class QRCodeController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping(value = "/generate-qr/{productId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQRCode(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        String qrUrl = product.getVideoUrl();
        if (qrUrl == null || qrUrl.isEmpty()) {
            qrUrl = "http://localhost:9090/product-details/" + productId;
        }
        ByteArrayOutputStream stream = QRCode
                .from(qrUrl)
                .withSize(250, 250)
                .to(ImageType.PNG)
                .stream();
        return stream.toByteArray();
    }
}