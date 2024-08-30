package com.thepetclub.ProductService.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Image;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.service.image.AmazonS3ClientService;
import com.thepetclub.ProductService.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final AmazonS3ClientService amazonS3ClientService;
    private final AmazonS3 amazonS3;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> saveImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productId") ObjectId productId) {
        try {
            List<ImageDto> imageDtos = imageService.saveImages(files, productId);
            return ResponseEntity.ok(new ApiResponse("upload successful", imageDtos));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("upload failed", e.getMessage()));
        }
    }

    @GetMapping("/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable ObjectId imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            Resource resource = amazonS3ClientService.downloadImageFromS3(image.getDownloadUrl());
            String contentType = image.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/image/{imageId}/update")
    public ResponseEntity<ApiResponse> updateImage(
            @PathVariable ObjectId imageId,
            @RequestBody MultipartFile file
    ) {
        try {
            Image existingImage = imageService.getImageById(imageId);
            if (existingImage != null) {
                amazonS3.deleteObject(bucketName, amazonS3ClientService.extractS3Key(existingImage.getDownloadUrl()));
                String s3Key = "images/" + imageId.toString() + "/" + file.getOriginalFilename();
                String newDownloadUrl = amazonS3ClientService.uploadFileToS3(file, s3Key);
                ImageDto updatedImageDto = imageService.updateImage(file, imageId, newDownloadUrl);
                if (updatedImageDto != null) {
                    return ResponseEntity.ok(new ApiResponse("update successful!", null));
                }
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("No image found with this id: " + imageId, null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("update failed!", INTERNAL_SERVER_ERROR));
    }

    @PutMapping("/image/{imageId}/delete")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable ObjectId imageId) {
        try {
            Image existingImage = imageService.getImageById(imageId);
            if (existingImage != null) {
                amazonS3.deleteObject(bucketName, amazonS3ClientService.extractS3Key(existingImage.getDownloadUrl()));
                imageService.deleteImageById(imageId);
                return ResponseEntity.ok(new ApiResponse("delete success!", null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("delete failed!", INTERNAL_SERVER_ERROR));
    }

}
