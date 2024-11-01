package com.example.foodstore.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/images")
public class ImageUploadController {

    // Đường dẫn đến thư mục lưu trữ hình ảnh
    @Value("${image.upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // Kiểm tra file rỗng
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Tạo tên file duy nhất để tránh trùng lặp
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + File.separator + fileName);

            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(path.getParent());

            // Lưu file vào thư mục đã chỉ định
            Files.write(path, file.getBytes());

            // Trả về tên file để lưu vào database
            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while uploading image");
        }
    }
}
