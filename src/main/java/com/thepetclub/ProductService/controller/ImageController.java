package com.thepetclub.ProductService.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.thepetclub.ProductService.clients.AuthService;
import com.thepetclub.ProductService.clients.AuthResponse;
import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.exception.UnauthorizedException;
import com.thepetclub.ProductService.model.Image;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.service.image.StorageService;
import com.thepetclub.ProductService.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("product/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final StorageService storageService;
    private final AmazonS3 amazonS3;
    private final AuthService authService;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> saveImages(
            @RequestBody List<MultipartFile> files,
            @RequestParam("productId") String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                List<ImageDto> imageDtos = imageService.saveImages(files, productId);
                return ResponseEntity.ok(new ApiResponse("Upload successful", imageDtos));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Upload failed", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Upload failed", e.getMessage()));
        }
    }


    @GetMapping("/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            Resource resource = storageService.downloadImageFromS3(image.getDownloadUrl());
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

    @GetMapping("/load/{imageId}")
    public ResponseEntity<Resource> loadImage(@PathVariable String imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            Resource resource = storageService.downloadImageFromS3(image.getDownloadUrl());
            String contentType = image.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PutMapping("/update/{imageId}")
    public ResponseEntity<ApiResponse> updateImage(
            @PathVariable String imageId,
            @RequestBody MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Image existingImage = imageService.getImageById(imageId);
                if (existingImage != null) {
                    amazonS3.deleteObject(bucketName, storageService.extractS3Key(existingImage.getDownloadUrl()));
                    String s3Key = "images/" + imageId + "/" + file.getOriginalFilename();
                    String newDownloadUrl = storageService.uploadFileToS3(file, s3Key);
                    ImageDto updatedImageDto = imageService.updateImage(file, imageId, newDownloadUrl);
                    if (updatedImageDto != null) {
                        return ResponseEntity.ok(new ApiResponse("Update successful!", updatedImageDto));
                    }
                }
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage() + imageId, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Update failed!", null));
    }


    @DeleteMapping("/delete/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(
            @PathVariable String imageId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Image existingImage = imageService.getImageById(imageId);
                if (existingImage != null) {
                    amazonS3.deleteObject(bucketName, storageService.extractS3Key(existingImage.getDownloadUrl()));
                    imageService.deleteImageById(imageId);
                    return ResponseEntity.ok(new ApiResponse("Delete success!", null));
                }
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Delete failed!", null));
    }


}
