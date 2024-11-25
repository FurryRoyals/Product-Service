package com.thepetclub.ProductService.request;

import lombok.Data;


@Data
public class ProductUpdateRequest {
    private String  id;
    private String name;
    private Double price;
    private int inventory;
    private String description;
    private String categoryName;
}

