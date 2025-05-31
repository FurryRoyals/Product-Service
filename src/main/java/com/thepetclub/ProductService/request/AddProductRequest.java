package com.thepetclub.ProductService.request;

import lombok.Data;
import org.bson.types.ObjectId;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    private String id;
    private String name;
    private Double price;
    private int discount;
    private Double discountedPrice;
    private int inventory;
    private String description;
    private String categoryName;
    private Boolean isFeatured;
}
