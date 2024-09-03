package com.thepetclub.ProductService.request;

import com.thepetclub.ProductService.model.Category;
import lombok.Data;
import org.bson.types.ObjectId;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String  id;
    private String name;
    private BigDecimal price;
    private int inventory;
    private String description;
    private String categoryName;
}

