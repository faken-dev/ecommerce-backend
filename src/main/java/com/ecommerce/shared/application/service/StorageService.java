package com.ecommerce.shared.application.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    /**
     * Uploads a file to the storage and returns the public URL.
     * @param file The file to upload.
     * @param folder The folder path (e.g., "products", "avatars").
     * @return The public URL of the uploaded file.
     * @throws IOException If an I/O error occurs.
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * Deletes a file from the storage.
     * @param fileUrl The full public URL of the file.
     */
    void deleteFile(String fileUrl);

    /**
     * Extracts the storage key from a full URL.
     */
    String getKeyFromUrl(String fileUrl);
}
