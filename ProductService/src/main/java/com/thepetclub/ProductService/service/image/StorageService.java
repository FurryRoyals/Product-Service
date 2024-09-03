package com.thepetclub.ProductService.service.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    @Autowired
    public StorageService(AmazonS3 amazonS3, @Value("${s3.bucket.name}") String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    public String uploadFileToS3(MultipartFile file, String s3Key) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucketName, s3Key, file.getInputStream(), metadata));
            return amazonS3.getUrl(bucketName, s3Key).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + file.getOriginalFilename(), e);
        }
    }

    public Resource downloadImageFromS3(String downloadUrl) {
        try {
            String s3Key = downloadUrl.replaceFirst("https://[^/]+/", "");

            S3Object s3Object = amazonS3.getObject(bucketName, s3Key);
            return new InputStreamResource(s3Object.getObjectContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download image from S3", e);
        }
    }

    public String extractS3Key(String downloadUrl) {
        return downloadUrl.replaceFirst("https://[^/]+/", "");
    }
}
