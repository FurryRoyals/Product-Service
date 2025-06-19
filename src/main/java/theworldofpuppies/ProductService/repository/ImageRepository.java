package theworldofpuppies.ProductService.repository;

import theworldofpuppies.ProductService.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {

}
