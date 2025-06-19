package theworldofpuppies.ProductService.repository;

import theworldofpuppies.ProductService.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Category findByName(String name);

    boolean existsByName(String name);
}
