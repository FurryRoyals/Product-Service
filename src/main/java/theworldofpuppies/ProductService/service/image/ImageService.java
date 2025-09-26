package theworldofpuppies.ProductService.service.image;

import theworldofpuppies.ProductService.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<Image> saveImages(List<MultipartFile> files, String productId, MultipartFile firstImage);
    Image updateImage(MultipartFile file, String productId, String oldS3Key);
    void deleteImageById(String s3Key, String productId);
    void deleteFirstImage(String s3Key, String productId);
}
