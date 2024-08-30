package com.thepetclub.ProductService.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class ImageDto {
    private ObjectId id;
    private String imageName;
    private String downloadUrl;
    private String thumbnailDownloadUrl;
}
