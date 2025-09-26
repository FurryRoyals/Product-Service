package theworldofpuppies.ProductService.service.product;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theworldofpuppies.ProductService.dto.ProductDto;
import theworldofpuppies.ProductService.exception.ResourceNotFoundException;
import theworldofpuppies.ProductService.model.Category;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.repository.CategoryRepository;
import theworldofpuppies.ProductService.repository.ProductRepository;
import theworldofpuppies.ProductService.request.AddProductRequest;
import theworldofpuppies.ProductService.request.ProductUpdateRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;

    private final String productNotFound = "No product found with: ";


    @Override
    public UpdateResult setProductField() {
        Query query = new Query();
        Update update = new Update().set("rating", 0.0);
        return mongoTemplate.updateMulti(query, update, Product.class);
    }

    @Override
    public ProductDto addProduct(AddProductRequest request) {
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategoryName()))
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(request.getCategoryName());
                    return categoryRepository.save(newCategory);
                });
        Product product = createProduct(request, request.getCategoryName());
        product.setCategoryName(category.getName());
        Product savedProduct = productRepository.save(product);

        List<String> productIds = category.getProductIds();
        productIds.add(savedProduct.getId());
        category.setProductIds(productIds);
        categoryRepository.save(category);
        return convertToDto(savedProduct);
    }

    @Transactional
    @Override
    public void addProductByCategory(List<AddProductRequest> requests, String category) {
        Category existingCategory = Optional.ofNullable(categoryRepository.findByName(category))
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(category);
                    return categoryRepository.save(newCategory);
                });
        List<Product> products = new ArrayList<>();
        requests.forEach(it -> {
            Product product = createProduct(it, category);
            products.add(product);
        });
        List<Product> savedProducts = productRepository.saveAll(products);
        List<String> productIds = existingCategory.getProductIds();
        savedProducts.forEach(it -> {
            productIds.add(it.getId());
        });
        existingCategory.setProductIds(productIds);
        categoryRepository.save(existingCategory);
    }

    private Product createProduct(AddProductRequest request, String category_name) {
        return new Product(
                request.getName(),
                request.getPrice(),
                request.getDiscount(),
                request.getDiscountedPrice(),
                request.getInventory(),
                request.getDescription(),
                category_name,
                request.getIsFeatured(),
                request.getIsRecommended()
        );
    }

    @Override
    public ProductDto getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + id));
        return convertToDto(product);
    }

    @Override
    public List<ProductDto> getProductsByIds(List<String> productIds) {
        List<Product> products = productRepository.findAllByIdIn(productIds);
        return products.stream().map(this::convertToDto).toList();
    }


    @Override
    public void deleteProductById(String id) throws ResourceNotFoundException {
        productRepository.findById(id).ifPresentOrElse(product -> {
            String categoryName = product.getCategoryName();
            Category category = Optional.ofNullable(categoryRepository.findByName(categoryName))
                    .orElseThrow(() -> new ResourceNotFoundException("No category found with: " + categoryName));
            category.getProductIds().removeIf(productId -> productId.equals(id));
            productRepository.deleteById(id);
        }, () -> {
            throw new ResourceNotFoundException(productNotFound + id);
        });
    }

    @Override
    public ProductDto updateProductById(ProductUpdateRequest request, String productId) {
        Product updatedProduct = productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + productId));

        return convertToDto(updatedProduct);
    }


    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        if (categoryRepository.existsByName(request.getCategoryName())) {
            throw new ResourceNotFoundException("No category found with: " + request.getCategoryName());
        }
        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setCategoryName(request.getCategoryName());
        existingProduct.setIsRecommended(request.getIsRecommended());
        productRepository.save(existingProduct);
        return existingProduct;
    }

    @Override
    public List<ProductDto> getAllProducts(String cursor, int size) {
        Query query = new Query();

        if (cursor != null && !cursor.isEmpty()) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(cursor)));
        }

        query.limit(size);

        // Step 1: Fetch products
        List<Product> products = mongoTemplate.find(query, Product.class);

        return products.stream().map(this::convertToDto).toList();
    }


    @Override
    public List<ProductDto> getFilteredProducts(String cursor, int size, String categoryName, String name) {
        Query query = new Query();

        if (cursor != null && !cursor.isEmpty()) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(cursor)));
        }
        if (categoryName != null && !categoryName.isEmpty()) {
            query.addCriteria(Criteria.where("categoryName").is(categoryName));
        }
        if (name != null && !name.isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(".*" + name + ".*", "i")); // case-insensitive
        }

        query.limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "_id"));

        // Step 1: Fetch products
        List<Product> products = mongoTemplate.find(query, Product.class);

        return products.stream().map(this::convertToDto).toList();
    }


    @Override
    public List<ProductDto> getAllFeaturedProducts() {
        try {
            // Step 1: Fetch featured products
            List<Product> products = productRepository.findByIsFeatured(true)
                    .orElse(Collections.emptyList());

            if (products.isEmpty()) {
                return Collections.emptyList();
            }

            return products.stream().map(this::convertToDto).toList();

        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve featured products", e);
        }
    }


    ProductDto convertToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

}
