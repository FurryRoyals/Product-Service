package theworldofpuppies.ProductService.service.category;

import theworldofpuppies.ProductService.exception.AlreadyExistsException;
import theworldofpuppies.ProductService.exception.ResourceNotFoundException;
import theworldofpuppies.ProductService.model.Category;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.repository.CategoryRepository;
import theworldofpuppies.ProductService.repository.ProductRepository;
import theworldofpuppies.ProductService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final MongoTemplate mongoTemplate;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    private final String categoryNotFound = "No category found with: ";

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(categoryNotFound + id));
    }

    @Override
    public Category getCategoryByName(String name) {
        return Optional.ofNullable(categoryRepository.findByName(name))
                .orElseThrow(() -> new ResourceNotFoundException(categoryNotFound + name));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(Category category) {
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException("category already exists!"));
    }

    @Transactional
    @Override
    public Category updateCategoryById(Category category, String id) {
        return Optional.ofNullable(getCategoryById(id))
                .map(oldCategory -> {

                    String oldCategoryName = oldCategory.getName();
                    oldCategory.setName(category.getName());

                    Query query = new Query(Criteria.where("categoryName").is(oldCategoryName));
                    Update update = new Update().set("categoryName", category.getName());
                    mongoTemplate.updateMulti(query, update, Product.class);

                    return categoryRepository.save(oldCategory);
                }).orElseThrow(() -> new ResourceNotFoundException(categoryNotFound + id));
    }


    @Override
    public void deleteCategoryById(String id) {
        categoryRepository.findById(id).ifPresentOrElse(category -> {
            for (String productId : category.getProductIds()) {
                productRepository.deleteById(productId);
            }
            categoryRepository.delete(category);
        }, () -> {
            throw new ResourceNotFoundException(categoryNotFound + id);
        });
    }

}
