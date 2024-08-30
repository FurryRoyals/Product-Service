package com.thepetclub.ProductService.service.product;

import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.request.AddProductRequest;
import com.thepetclub.ProductService.request.ProductUpdateRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface ProductService {
    Product addProduct(AddProductRequest request);
    Product getProductById(ObjectId id);
    void deleteProductById(ObjectId id);
    Product updateProductById(ProductUpdateRequest request, ObjectId id);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByName(String name);
}
