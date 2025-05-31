package com.thepetclub.ProductService.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private Double price;
    private int discount;
    private Double discountedPrice;
    private int inventory;
    private String description;
    private String categoryName;

    private String firstImageId;
    private Boolean isFeatured;

    private List<String> imageIds = new ArrayList<>();

    public Product(String name, Double price, int discount, Double discountedPrice, int inventory, String description, String categoryName, Boolean isFeatured) {
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.discountedPrice = discountedPrice;
        this.inventory = inventory;
        this.description = description;
        this.categoryName = categoryName;
        this.isFeatured = isFeatured;
    }
}
