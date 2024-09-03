package com.thepetclub.ProductService.dto;

import lombok.Data;

@Data
public class ImageDto {
    private String id;
    private String fileName;
    private String fetchUrl;
    private String productId;
}
