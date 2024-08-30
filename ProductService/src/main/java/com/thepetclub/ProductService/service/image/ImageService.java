package com.thepetclub.ProductService.service.image;

import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.model.Image;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<ImageDto> saveImages(List<MultipartFile> files, ObjectId productId);
    ImageDto updateImage(MultipartFile file, ObjectId imageId, String newDownloadUrl);
    Image getImageById(ObjectId id);
    List<Image> getImagesByProductId(String productId);
    void deleteImageById(ObjectId id);
}
