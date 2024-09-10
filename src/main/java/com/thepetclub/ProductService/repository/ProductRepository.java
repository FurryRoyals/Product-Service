package com.thepetclub.ProductService.repository;

import com.thepetclub.ProductService.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategoryName(String categoryName);

    List<Product> findByName(String name);
}
