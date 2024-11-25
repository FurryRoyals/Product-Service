package com.thepetclub.ProductService.service.image;

import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<ImageDto> saveImages(List<MultipartFile> files, String productId, MultipartFile firstImage);
    ImageDto updateImage(MultipartFile file, String imageId, String newDownloadUrl);
    Image getImageById(String id);
    List<Image> getImagesByProductId(String productId);
    void deleteImageById(String id);
}
