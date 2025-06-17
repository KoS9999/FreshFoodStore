package com.example.foodstore.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private final Storage storage;

    @Value("${firebase.bucket.name}")
    private String bucketName;

    public FirebaseStorageService(@Value("${firebase.config.path}") String firebaseConfigPath) throws Exception {
        try {
            // Đọc file từ hệ thống tệp thay vì classpath
            //InputStream serviceAccount = new FileInputStream("/app/firebase/firebase-config.json");
            InputStream serviceAccount = new FileInputStream("/etc/secrets/firebase-config.json");
            Credentials credentials = GoogleCredentials.fromStream(serviceAccount);
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Firebase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể khởi tạo FirebaseStorageService", e);
        }
    }

    public String uploadFileReviews(MultipartFile file) {
        return upload(file, "review/");
    }

    public String uploadFileBlogs(MultipartFile file) {
        return upload(file, "blogs/");
    }

    private String upload(MultipartFile file, String folder) {
        try {
            String fileName = folder + UUID.randomUUID() + "_" + file.getOriginalFilename();

            Blob blob = storage.create(
                    BlobInfo.newBuilder(bucketName, fileName)
                            .setContentType(file.getContentType())
                            .build(),
                    file.getInputStream()
            );

            return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/"
                    + fileName.replace("/", "%2F") + "?alt=media";

        } catch (Exception e) {
            System.err.println("Lỗi upload file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1).split("\\?")[0];
            BlobId blobId = BlobId.of(bucketName, fileName);
            return storage.delete(blobId);
        } catch (Exception e) {
            System.err.println("Lỗi xóa file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}