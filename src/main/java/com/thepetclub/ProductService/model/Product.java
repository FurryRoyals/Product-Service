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
    private BigDecimal price;
    private int inventory;
    private String description;

    private String categoryName;

    private List<String> imageIds = new ArrayList<>();

    public Product(String name, BigDecimal price, int inventory, String description, String categoryName) {
        this.name = name;
        this.price = price;
        this.inventory = inventory;
        this.description = description;
        this.categoryName = categoryName;
    }
}
