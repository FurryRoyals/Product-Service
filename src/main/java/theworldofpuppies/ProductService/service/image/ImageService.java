package theworldofpuppies.ProductService.service.image;

import theworldofpuppies.ProductService.dto.ImageDto;
import theworldofpuppies.ProductService.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<ImageDto> saveImages(List<MultipartFile> files, String productId, MultipartFile firstImage);
    ImageDto updateImage(MultipartFile file, String imageId, String newDownloadUrl);
    Image getImageById(String id);
    List<Image> getImagesByProductId(String productId);
    void deleteImageById(String id);
}
