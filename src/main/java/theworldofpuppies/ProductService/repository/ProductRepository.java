package theworldofpuppies.ProductService.repository;

import theworldofpuppies.ProductService.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    Page<Product> findByName(String name, Pageable pageable);
    Optional<List<Product>> findByIsFeatured(Boolean isFeatured);
}
