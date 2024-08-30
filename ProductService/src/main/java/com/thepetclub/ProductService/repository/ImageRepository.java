package com.thepetclub.ProductService.repository;

import com.thepetclub.ProductService.model.Image;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends MongoRepository<Image, ObjectId> {

}
