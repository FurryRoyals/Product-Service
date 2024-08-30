package com.thepetclub.ProductService.repository;

import com.thepetclub.ProductService.model.Category;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, ObjectId> {
    Category findByName(String name);

    boolean existsByName(String name);
}
