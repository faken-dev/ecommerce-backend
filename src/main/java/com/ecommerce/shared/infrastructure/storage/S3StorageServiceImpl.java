package com.ecommerce.shared.infrastructure.storage;

import com.ecommerce.shared.application.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final S3Client s3Client;

    @Value("${notification.storage.r2.bucket-name}")
    private String bucketName;

    @Value("${notification.storage.r2.public-url}")
    private String publicUrl;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String key = folder + "/" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ) // Note: R2 might ignore this if bucket is private, but good to have
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("Uploaded file to R2: {}", key);
        
        // Return public URL: https://public-url.com/folder/fileName
        return String.format("%s/%s", publicUrl.replaceAll("/$", ""), key);
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String key = getKeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file from R2: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", fileUrl, e);
        }
    }

    @Override
    public String getKeyFromUrl(String fileUrl) {
        if (fileUrl == null) return null;
        return fileUrl.replace(publicUrl.replaceAll("/$", ""), "").replaceFirst("^/", "");
    }
}
