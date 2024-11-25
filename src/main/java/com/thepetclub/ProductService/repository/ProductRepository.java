package com.thepetclub.ProductService.repository;

import com.thepetclub.ProductService.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    Page<Product> findByName(String name, Pageable pageable);
}
