package com.thepetclub.ProductService.service.category;

import com.thepetclub.ProductService.model.Category;
import org.bson.types.ObjectId;

import java.util.List;

public interface CategoryService {
    Category getCategoryById(ObjectId id);
    Category getCategoryByName(String name);
    List<Category> getAllCategories();
    Category addCategory(Category category);
    Category updateCategoryById(Category category, ObjectId id);
    void deleteCategoryById(ObjectId id);
}
