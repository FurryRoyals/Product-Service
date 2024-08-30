package com.thepetclub.ProductService.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "categories")
public class Category {
    @Id
    private ObjectId id;
    private String name;

    private List<ObjectId> productIds = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }
}

