package com.thepetclub.ProductService.service.product;

import com.thepetclub.ProductService.controller.ImageController;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Category;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.CategoryRepository;
import com.thepetclub.ProductService.repository.ImageRepository;
import com.thepetclub.ProductService.repository.ProductRepository;
import com.thepetclub.ProductService.request.AddProductRequest;
import com.thepetclub.ProductService.request.ProductUpdateRequest;
import com.thepetclub.ProductService.service.category.CategoryService;
import com.thepetclub.ProductService.service.category.CategoryServiceImpl;
import com.thepetclub.ProductService.service.image.ImageService;
import com.thepetclub.ProductService.service.image.ImageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    //    private final ImageService imageService;
//    private final CategoryService categoryService;
    private final ImageRepository imageRepository;

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

//    @Override
//    public void deleteProductById(String id) {
//        Product product = getProductById(id);
//        String categoryName = product.getCategoryName();
//        productRepository.delete(product);
//        Category category = Optional.ofNullable(categoryRepository.findByName(categoryName))
//                .orElseThrow(() -> new ResourceNotFoundException("No Category found with: " + categoryName));
//        category.getProductIds().removeIf(productId -> productId.equals(id));
//        categoryRepository.save(category);
//    }


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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        List<Product> products = productRepository.findByCategoryName(categoryName);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(productNotFound + categoryName);
        }
        return products;
    }

    @Override
    public List<Product> getProductsByName(String name) {
        List<Product> products = productRepository.findByName(name);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(productNotFound + name);
        }
        return products;
    }
}
