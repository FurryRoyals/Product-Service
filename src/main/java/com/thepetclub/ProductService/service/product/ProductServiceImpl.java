package com.thepetclub.ProductService.service.product;

import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Category;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.CategoryRepository;
import com.thepetclub.ProductService.repository.ImageRepository;
import com.thepetclub.ProductService.repository.ProductRepository;
import com.thepetclub.ProductService.request.AddProductRequest;
import com.thepetclub.ProductService.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final MongoTemplate mongoTemplate;

    private final String productNotFound = "No product found with: ";

    @Override
    public Product addProduct(AddProductRequest request) {
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
        return savedProduct;
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
        requests.forEach( it -> {
            Product product = createProduct(it, category);
            products.add(product);
        });
        List<Product> savedProducts = productRepository.saveAll(products);
        List<String> productIds = existingCategory.getProductIds();
        savedProducts.forEach( it -> {
            productIds.add(it.getId());
        });
        existingCategory.setProductIds(productIds);
        categoryRepository.save(existingCategory);
    }

    private Product createProduct(AddProductRequest request, String category_name) {
        return new Product(
                request.getName(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category_name
        );
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + id));
    }



    @Override
    public void deleteProductById(String id) throws ResourceNotFoundException {
        productRepository.findById(id).ifPresentOrElse(product -> {
            String categoryName = product.getCategoryName();
            Category category = Optional.ofNullable(categoryRepository.findByName(categoryName))
                    .orElseThrow(() -> new ResourceNotFoundException("No category found with: " + categoryName));
            category.getProductIds().removeIf(productId -> productId.equals(id));
            productRepository.deleteById(id);
            for (String imageId : product.getImageIds()) {
                imageRepository.findById(imageId).ifPresentOrElse(image -> imageRepository.deleteById(imageId),
                        () -> {
                            throw new RuntimeException("No image found with " + imageId);
                        }
                );
            }
        }, () -> {
            throw new ResourceNotFoundException(productNotFound + id);
        });
    }

    @Override
    public Product updateProductById(ProductUpdateRequest request, String productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + productId));
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
        productRepository.save(existingProduct);
        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts(String cursor, int size) {
        Query query = new Query();

        if (cursor != null && !cursor.isEmpty()) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(cursor)));
        }

        query.limit(size);
        return mongoTemplate.find(query, Product.class);
    }

    @Override
    public List<Product> getFilteredProducts(String cursor, int size, String categoryName, String name) {
        Query query = new Query();

        if (cursor != null && !cursor.isEmpty()) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(cursor)));
        }
        if (categoryName != null) {
            query.addCriteria(Criteria.where("categoryName").is(categoryName));
        }
        if (name != null) {
            query.addCriteria(Criteria.where("name").regex(".*" + name + ".*", "i"));
        }

        query.limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "_id"));

        return mongoTemplate.find(query, Product.class);
    }

}
