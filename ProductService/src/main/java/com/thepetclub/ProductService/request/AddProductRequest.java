package com.thepetclub.ProductService.request;

import lombok.Data;
import org.bson.types.ObjectId;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    private ObjectId id;
    private String name;
    private BigDecimal price;
    private int inventory;
    private String description;
    private String categoryName;
}
