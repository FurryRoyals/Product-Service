package com.thepetclub.ProductService.service.category;

import com.thepetclub.ProductService.exception.AlreadyExistsException;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Category;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.CategoryRepository;
import com.thepetclub.ProductService.repository.ProductRepository;
import com.thepetclub.ProductService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    private final String categoryNotFound = "No category found with: ";

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(categoryNotFound + id));
    }

    @Override
    public Category getCategoryByName(String name) {
        return Optional.ofNullable(categoryRepository.findByName(name))
                .orElseThrow(()-> new ResourceNotFoundException(categoryNotFound + name));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(Category category) {
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(()-> new AlreadyExistsException("category already exists!"));
    }

    @Override
    public Category updateCategoryById(Category category, String id) {
        return Optional.ofNullable(getCategoryById(id))
                .map(oldCategory -> {
                    oldCategory.setName(category.getName());
                    for (String productId : oldCategory.getProductIds()) {
                        Product product = productService.getProductById(productId);
                        product.setCategoryName(category.getName());
                        productRepository.save(product);
                    }
                    return categoryRepository.save(oldCategory);
                }).orElseThrow(()-> new ResourceNotFoundException(categoryNotFound + id));
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
