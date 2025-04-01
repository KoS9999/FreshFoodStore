package com.example.foodstore.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private final Storage storage;
    private final String bucketName = "foodstore-1cd36.firebasestorage.app";

    public FirebaseStorageService() throws IOException {
        InputStream serviceAccount = new FileInputStream("src/main/resources/foodstore-1cd36-firebase-adminsdk-fbsvc-02a0299bae.json");
        Credentials credentials = GoogleCredentials.fromStream(serviceAccount);
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    }

    public String uploadFile(MultipartFile file) {
        System.out.println("🔍 Đang upload ảnh lên Firebase: " + file.getOriginalFilename());

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            Blob blob = storage.create(
                    BlobInfo.newBuilder(bucketName, fileName)
                            .setContentType(file.getContentType())
                            .build(),
                    file.getInputStream()
            );

            String fileUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/"
                    + fileName.replaceAll("/", "%2F") + "?alt=media";

            System.out.println("Ảnh upload thành công: " + fileUrl);
            return fileUrl;
        } catch (Exception e) {
            System.err.println("Lỗi khi upload ảnh lên Firebase: " + e.getMessage());
            return null;
        }
    }
    public String uploadFileBlogs(MultipartFile file) {
        System.out.println("🔍 Đang upload ảnh lên Firebase: " + file.getOriginalFilename());

        try {
            String folder = "blogs/";
            String fileName = folder + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            Blob blob = storage.create(
                    BlobInfo.newBuilder(bucketName, fileName)
                            .setContentType(file.getContentType())
                            .build(),
                    file.getInputStream()
            );

            String fileUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/"
                    + fileName.replaceAll("/", "%2F") + "?alt=media";

            System.out.println("Ảnh upload thành công: " + fileUrl);
            return fileUrl;
        } catch (Exception e) {
            System.err.println("Lỗi khi upload ảnh lên Firebase: " + e.getMessage());
            return null;
        }
    }



    public boolean deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1).split("\\?")[0]; // Lấy tên file từ URL
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                System.out.println("Đã xóa ảnh: " + fileName);
            } else {
                System.out.println("Ảnh không tồn tại hoặc không xóa được: " + fileName);
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
            return false;
        }
    }

}
