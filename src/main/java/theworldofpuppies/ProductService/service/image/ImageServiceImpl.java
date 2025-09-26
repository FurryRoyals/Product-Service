package theworldofpuppies.ProductService.service.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.ProductService.exception.ResourceNotFoundException;
import theworldofpuppies.ProductService.model.Image;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.repository.ProductRepository;
import theworldofpuppies.ProductService.utils.ImageCompressionUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final StorageService storageService;
    private final ImageCompressionUtil imageCompressionUtil;
    private final ProductRepository productRepository;

    private final String imageNotFound = "No image found with: ";

    @Transactional
    @Override
    public List<Image> saveImages(List<MultipartFile> files, String productId, MultipartFile firstImage) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(imageNotFound + productId));

        List<Image> images = new ArrayList<>();

        if (firstImage != null) {
            try {
                String s3Key = "images/" + productId + "/" + firstImage.getOriginalFilename();
                storageService.uploadFileToS3(firstImage, s3Key);

                Image firstImageEntity = new Image();
                firstImageEntity.setFileName(firstImage.getOriginalFilename());
                firstImageEntity.setFileType(firstImage.getContentType());
                firstImageEntity.setS3Key(s3Key);
                firstImageEntity.setFetchUrl(storageService.generatePresignedUrl(s3Key));

                product.setFirstImage(firstImageEntity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save the first image: " + firstImage.getOriginalFilename(), e);
            }
        }

        if (files != null) {
            for (MultipartFile file : files) {
                try {

                    String s3Key = "images/" + productId + "/" + file.getOriginalFilename();
                    storageService.uploadFileToS3(file, s3Key);

                    Image image = new Image();
                    image.setFileName(file.getOriginalFilename());
                    image.setFileType(file.getContentType());
                    image.setS3Key(s3Key);
                    image.setFetchUrl(storageService.generatePresignedUrl(s3Key));
                    images.add(image);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save image: " + file.getOriginalFilename(), e);
                }
            }
        }

        product.setImages(images);
        productRepository.save(product);
        return images;
    }

    @Transactional
    @Override
    public Image updateImage(MultipartFile file, String productId, String oldS3Key) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id: " + productId));

            // Delete old file from S3 (optional, only if you don’t want to keep it)
            storageService.deleteFileFromS3(oldS3Key);

            // Upload new file
            String newS3Key = "images/" + productId + "/" + file.getOriginalFilename();
            storageService.uploadFileToS3(file, newS3Key);

            // Create new image object
            Image newImage = new Image();
            newImage.setFileName(file.getOriginalFilename());
            newImage.setFileType(file.getContentType());
            newImage.setS3Key(newS3Key);
            newImage.setFetchUrl(storageService.generatePresignedUrl(newS3Key));

            // Replace old image in product
            List<Image> updatedImages = new ArrayList<>(product.getImages().stream()
                    .filter(img -> !img.getS3Key().equals(oldS3Key)) // remove old one
                    .toList());
            updatedImages.add(newImage);
            product.setImages(updatedImages);

            // Save both product & image
            productRepository.save(product);
            return newImage;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to update image: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void deleteImageById(String s3Key, String productId) {
        Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id: " + productId));

        List<Image> images = product.getImages();
        List<Image> updatedImages = images.stream().filter(img -> !img.getS3Key().equals(s3Key))
                .toList();
        storageService.deleteFileFromS3(s3Key);
        product.setImages(updatedImages);
        productRepository.save(product);
    }

    @Transactional
    @Override
    public void deleteFirstImage(String s3Key, String productId) {
        Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with this id: " + productId));
        storageService.deleteFileFromS3(s3Key);
        product.setFirstImage(null);
        productRepository.save(product);
    }
}
