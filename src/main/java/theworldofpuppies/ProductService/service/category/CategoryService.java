package theworldofpuppies.ProductService.service.category;

import theworldofpuppies.ProductService.model.Category;

import java.util.List;

public interface CategoryService {
    Category getCategoryById(String  id);
    Category getCategoryByName(String name);
    List<Category> getAllCategories();
    Category addCategory(Category category);
    Category updateCategoryById(Category category, String id);
    void deleteCategoryById(String id);
}
