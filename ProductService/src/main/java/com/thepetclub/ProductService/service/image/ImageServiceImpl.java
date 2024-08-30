package com.thepetclub.ProductService.service.image;

import com.amazonaws.services.s3.AmazonS3;
import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Image;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.ImageRepository;
import com.thepetclub.ProductService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductService productService;
    private final AmazonS3ClientService amazonS3ClientService;
    private final AmazonS3 amazonS3;


    private final String imageNotFound = "No image found with: ";

    @Override
    public List<ImageDto> saveImages(List<MultipartFile> files, ObjectId productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ResourceNotFoundException(imageNotFound + productId);
        }

        List<ImageDto> savedImageDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                // Upload the original image to Amazon S3
                String s3Key = "images/" + productId.toString() + "/" + file.getOriginalFilename();
                String downloadUrl = amazonS3ClientService.uploadFileToS3(file, s3Key); // Assume this method uploads to S3 and returns the file's URL


                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setThumbnail(generateThumbnail(file.getBytes()));
                image.setProductId(productId);
                image.setDownloadUrl(downloadUrl);

                String buildThumbnailDownloadUrl = "/api/v1/images/image/download/";
                Image savedImage = imageRepository.save(image);
                String thumbnailDownloadUrl = buildThumbnailDownloadUrl + savedImage.getId();
                savedImage.setThumbnailDownloadUrl(thumbnailDownloadUrl);
                imageRepository.save(savedImage);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setImageName(savedImage.getFileName());
                imageDto.setThumbnailDownloadUrl(savedImage.getThumbnailDownloadUrl());
                imageDto.setDownloadUrl(downloadUrl);
                savedImageDtos.add(imageDto);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image: " + file.getOriginalFilename(), e);
            }
        }
        return savedImageDtos;
    }

    @Override
    public ImageDto updateImage(MultipartFile file, ObjectId imageId, String newDownloadUrl) {
        try {
            Image image = getImageById(imageId);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setDownloadUrl(newDownloadUrl);
            byte[] fileBytes = file.getBytes();
            byte[] thumbnail = generateThumbnail(fileBytes);
            image.setThumbnail(thumbnail);
            Image updatedImage = imageRepository.save(image);

            ImageDto imageDto = new ImageDto();
            imageDto.setId(updatedImage.getId());
            imageDto.setImageName(updatedImage.getFileName());
            imageDto.setThumbnailDownloadUrl(updatedImage.getThumbnailDownloadUrl());
            imageDto.setDownloadUrl(updatedImage.getDownloadUrl());
            return imageDto;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Image getImageById(ObjectId id) {
        return imageRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(imageNotFound + id));
    }

    @Override
    public List<Image> getImagesByProductId(String productId) {
        return List.of();
    }

    @Override
    public void deleteImageById(ObjectId id) {
        imageRepository.findById(id).ifPresentOrElse(imageRepository::delete,
                ()-> {throw new ResourceNotFoundException(imageNotFound + id);
                });
    }

    private byte[] generateThumbnail(byte[] imageBytes) throws IOException {
        // Convert byte array to BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage originalImage = ImageIO.read(bais);

        // Create a scaled instance for the thumbnail
        int thumbnailWidth = 100;  // Set your desired thumbnail width
        int thumbnailHeight = (thumbnailWidth * originalImage.getHeight()) / originalImage.getWidth();
        BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, originalImage.getType());

        // Draw the scaled instance
        Graphics2D g = thumbnailImage.createGraphics();
        g.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        g.dispose();

        // Convert BufferedImage back to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", baos);  // Change "jpg" to your image format
        return baos.toByteArray();
    }
}
