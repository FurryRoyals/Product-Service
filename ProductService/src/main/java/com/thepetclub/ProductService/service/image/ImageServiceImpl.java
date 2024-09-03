package com.thepetclub.ProductService.service.image;

import com.thepetclub.ProductService.dto.ImageDto;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Image;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.ImageRepository;
import com.thepetclub.ProductService.repository.ProductRepository;
import com.thepetclub.ProductService.service.product.ProductService;
import com.thepetclub.ProductService.utils.ImageCompressionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductService productService;
    private final StorageService storageService;
    private final ImageCompressionUtil imageCompressionUtil;
    private final ProductRepository productRepository;

    private final String imageNotFound = "No image found with: ";


    @Override
    public List<ImageDto> saveImages(List<MultipartFile> files, String productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ResourceNotFoundException(imageNotFound + productId);
        }

        List<ImageDto> savedImageDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String s3Key = "images/" + productId + "/" + file.getOriginalFilename();
                String downloadUrl = storageService.uploadFileToS3(file, s3Key);

                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());

                String fetchUrl = "api/v1/images/load/";

                image.setFetchUrl(fetchUrl);
                image.setProductId(productId);
                image.setDownloadUrl(downloadUrl);
                Image saved = imageRepository.save(image);

                saved.setFetchUrl(fetchUrl + saved.getId());
                Image savedImage = imageRepository.save(saved);

                List<String> imageIds = product.getImageIds();
                imageIds.add(savedImage.getId());
                product.setImageIds(imageIds);
                productRepository.save(product);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setFileName(savedImage.getFileName());
                imageDto.setFetchUrl(savedImage.getFetchUrl());
                imageDto.setProductId(productId);
                savedImageDtos.add(imageDto);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save image: " + file.getOriginalFilename(), e);
            }
        }
        return savedImageDtos;
    }

    @Override
    public ImageDto updateImage(MultipartFile file, String imageId, String newDownloadUrl) {
        try {
            Image image = getImageById(imageId);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setDownloadUrl(newDownloadUrl);
            Image updatedImage = imageRepository.save(image);

            ImageDto imageDto = new ImageDto();
            imageDto.setId(updatedImage.getId());
            imageDto.setFileName(updatedImage.getFileName());
            imageDto.setFetchUrl(updatedImage.getFetchUrl());
            return imageDto;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Image getImageById(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(imageNotFound + id));
    }

    @Override
    public List<Image> getImagesByProductId(String productId) {
        return List.of();
    }

    @Override
    public void deleteImageById(String imageId) {
        imageRepository.findById(imageId).ifPresentOrElse(image -> imageRepository.deleteById(imageId),
                () -> {
                    throw new RuntimeException(imageNotFound + imageId);
                }
        );
    }
}
