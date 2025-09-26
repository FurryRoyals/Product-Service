package theworldofpuppies.ProductService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.ProductService.clients.AuthResponse;
import theworldofpuppies.ProductService.clients.AuthService;
import theworldofpuppies.ProductService.exception.ResourceNotFoundException;
import theworldofpuppies.ProductService.exception.UnauthorizedException;
import theworldofpuppies.ProductService.model.Image;
import theworldofpuppies.ProductService.response.ApiResponse;
import theworldofpuppies.ProductService.service.image.ImageService;
import theworldofpuppies.ProductService.service.image.StorageService;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("${prefix}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final AuthService authService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> saveImages(
            @RequestBody(required = false) List<MultipartFile> files,
            @RequestBody(required = false) MultipartFile firstImage,
            @RequestParam("productId") String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                List<Image> imageDtos = imageService.saveImages(files, productId, firstImage);
                return ResponseEntity.ok(new ApiResponse("Upload successful", true, imageDtos));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Upload failed", false, e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Upload failed", false, e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateImage(
            @RequestParam String s3Key,
            @RequestParam String productId,
            @RequestBody MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Image image = imageService.updateImage(file, productId, s3Key);
                return ResponseEntity.ok(new ApiResponse("Updated successfully", true, image));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage() + s3Key, false, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Update failed!", false, null));
        }
    }

    @DeleteMapping("/delete/")
    public ResponseEntity<ApiResponse> deleteImage(
            @RequestParam String s3Key,
            @RequestParam String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {

                imageService.deleteImageById(s3Key, productId);
                return ResponseEntity.ok(new ApiResponse("Delete success!", false, null));

            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Delete failed!", false, null));
        }
    }

    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse> saveImagesWithoutAuth(
            @RequestBody(required = false) List<MultipartFile> files,
            @RequestBody(required = false) MultipartFile firstImage,
            @RequestParam("productId") String productId) {
        try {
            List<Image> images = imageService.saveImages(files, productId, firstImage);
            return ResponseEntity.ok(new ApiResponse("Upload successful", true, images));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Upload failed", false, e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Upload failed", false, e.getMessage()));
        }
    }

}
